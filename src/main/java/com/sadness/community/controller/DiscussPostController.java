package com.sadness.community.controller;

import com.sadness.community.annotation.LoginRequired;
import com.sadness.community.entity.*;
import com.sadness.community.event.EventProducer;
import com.sadness.community.service.CommentService;
import com.sadness.community.service.DiscussPostService;
import com.sadness.community.service.LikeService;
import com.sadness.community.service.UserService;
import com.sadness.community.util.CommunityConstant;
import com.sadness.community.util.CommunityUtil;
import com.sadness.community.util.HostHolder;
import com.sadness.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @version 1.0
 * @Date 2022/6/9 11:29
 * @Author SadAndBeautiful
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 添加帖子
     */
    @LoginRequired
    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登陆，请先登录");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());

        discussPostService.addDiscussPost(discussPost);

        // 触发事件，将贴子存储到es中
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

        // 将贴子存入redis等待计算分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, discussPost.getId());

        return CommunityUtil.getJSONString(0, "发布成功");
    }

    /**
     * 显示帖子详情，包含帖子评论，评论的评论
     */
    @GetMapping("/detail/{postId}")
    public String getDiscussPost(@PathVariable("postId") int postId, Model model, Page page) {
        // 查询帖子
        DiscussPost post = discussPostService.findDiscussPost(postId);
        model.addAttribute("post", post);
        // 查询帖子作者
        User user = userService.getUserById(post.getUserId());
        model.addAttribute("user", user);
        // 查询帖子点赞数量
        long likeCount = likeService.findLikeCount(ENTITY_TYPE_POST, postId);
        model.addAttribute("likeCount", likeCount);

        // 当前用户点赞状态
        User loginUser = hostHolder.getUser();
        if (loginUser != null) {
            model.addAttribute("likeStatus", likeService.findUserLikeStatus(loginUser.getId(), ENTITY_TYPE_POST, postId));
        } else {
            model.addAttribute("likeStatus", 0);
        }

        // 设置分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + postId);
        page.setRows(post.getCommentCount());

        // 查询帖子所有评论
        List<Comment> commentList =
                commentService.findCommentsByEntity(ENTITY_TYPE_POST, postId, page.getOffset(), page.getLimit());

        // 帖子评论的view object列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();

        if (commentList != null) {
            // 遍历帖子的评论
            for (Comment comment : commentList) {
                // 把每个评论的信息放入commentVo，最终放入commentVoList
                Map<String, Object> commentVo = new HashMap<>();
                // 当前评论的信息
                commentVo.put("comment", comment);
                // 当前评论的作者的信息
                commentVo.put("user", userService.getUserById(comment.getUserId()));
                // 当前评论点赞数量
                commentVo.put("likeCount", likeService.findLikeCount(ENTITY_TYPE_COMMENT, comment.getId()));
                // 当前用户点赞状态
                if (loginUser != null) {
                    commentVo.put("likeStatus", likeService.findUserLikeStatus(loginUser.getId(), ENTITY_TYPE_COMMENT, comment.getId()));
                } else {
                    commentVo.put("likeStatus", 0);
                }

                // 查询当前评论的回复，不分页
                List<Comment> replyList =
                        commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 评论回复的view object列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();

                if (replyList != null) {
                    for (Comment reply : replyList) {
                        // 把每个回复的信息放入replyVo，最终放入replyVoList
                        Map<String, Object> replyVo = new HashMap<>();
                        // 当前回复的信息
                        replyVo.put("reply", reply);
                        // 当前回复的作者的信息
                        replyVo.put("user", userService.getUserById(reply.getUserId()));
                        // 当前回复点赞数量
                        replyVo.put("likeCount", likeService.findLikeCount(ENTITY_TYPE_COMMENT, reply.getId()));
                        // 当前用户点赞状态
                        if (loginUser != null) {
                            replyVo.put("likeStatus", likeService.findUserLikeStatus(loginUser.getId(), ENTITY_TYPE_COMMENT, reply.getId()));
                        } else {
                            replyVo.put("likeStatus", 0);
                        }
                        // 回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.getUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        replyVoList.add(replyVo);
                    }
                }

                commentVo.put("replies", replyVoList);

                // 评论回复的数量
                int replyCount = commentService.findCommentCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return "site/discuss-detail";
    }

    /**
     * 帖子置顶
     */
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int postId) {
        // 修改帖子类型
        discussPostService.updateType(postId, 1);
        // 触发事件，将修改后的帖子重新储存到es中
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(postId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "置顶成功！");
    }


    /**
     * 帖子加精
     */
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int postId) {
        // 修改帖子类型
        discussPostService.updateStatus(postId, 1);
        // 触发事件，将修改后的帖子重新储存到es中
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(postId);
        eventProducer.fireEvent(event);

        // 将贴子存入redis等待计算分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, postId);

        return CommunityUtil.getJSONString(0, "加精成功！");
    }

    /**
     * 帖子删除
     */
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int postId) {
        // 修改帖子类型
        discussPostService.updateStatus(postId, 2);
        // 触发事件，es中的帖子删除
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(postId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "删除成功！");
    }


}
