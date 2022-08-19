package com.sadness.community.controller;

import com.sadness.community.annotation.LoginRequired;
import com.sadness.community.entity.Event;
import com.sadness.community.entity.Page;
import com.sadness.community.entity.User;
import com.sadness.community.event.EventProducer;
import com.sadness.community.service.FollowService;
import com.sadness.community.service.UserService;
import com.sadness.community.util.CommunityConstant;
import com.sadness.community.util.CommunityUtil;
import com.sadness.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2022/6/14 23:06
 * @Author SadAndBeautiful
 */
@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    /**
     * 关注
     */
    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);

        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "关注成功！");
    }

    /**
     * 取消关注
     */
    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "取消关注成功！");
    }

    /**
     * 响应关注列表
     */
    @LoginRequired
    @GetMapping("/followee/{userId}")
    public String getFollowee(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在！");
        }
        model.addAttribute("user", user);

        // 设置分页属性
        page.setLimit(5);
        page.setPath("/followee/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        // 查询用户关注的人
        List<Map<String, Object>> followeeList = followService.findUserFollowee(userId, page.getOffset(), page.getLimit());
        if (followeeList != null) {
            User loginUser = hostHolder.getUser();
            for (Map<String, Object> map : followeeList) {
                // 登录用户是否关注该用户
                User followeeUser = (User) map.get("followee");
                boolean hasFollowed = followService.isFollow(loginUser.getId(), ENTITY_TYPE_USER, followeeUser.getId());
                map.put("hasFollowed", hasFollowed);
            }
        }
        model.addAttribute("followee", followeeList);
        return "/site/followee";
    }

    /**
     * 响应粉丝列表
     */
    @LoginRequired
    @GetMapping("/follower/{userId}")
    public String getFollower(@PathVariable("userId") Integer userId, Page page, Model model) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在！");
        }
        model.addAttribute("user", user);

        // 设置分页属性
        page.setLimit(5);
        page.setPath("/follower/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        // 查询用户的粉丝
        List<Map<String, Object>> followerList = followService.findUserFollower(userId, page.getOffset(), page.getLimit());
        if (followerList != null) {
            User loginUser = hostHolder.getUser();
            for (Map<String, Object> map : followerList) {
                // 登录用户是否关注该用户
                User followerUser = (User) map.get("follower");
                boolean hasFollowed = followService.isFollow(loginUser.getId(), ENTITY_TYPE_USER, followerUser.getId());
                map.put("hasFollowed", hasFollowed);
            }
        }
        model.addAttribute("follower", followerList);
        return "/site/follower";
    }
}
