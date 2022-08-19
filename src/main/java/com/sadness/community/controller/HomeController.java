package com.sadness.community.controller;

import com.sadness.community.dao.DiscussPostMapper;
import com.sadness.community.dao.UserMapper;
import com.sadness.community.entity.DiscussPost;
import com.sadness.community.entity.Page;
import com.sadness.community.entity.User;
import com.sadness.community.service.DiscussPostService;
import com.sadness.community.service.LikeService;
import com.sadness.community.service.MessageService;
import com.sadness.community.util.CommunityConstant;
import com.sadness.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2022/6/1 11:22
 * @Author SadAndBeautiful
 */
@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;


    /**
     * 处理根路径
     */
    @GetMapping("/")
    public String root() {
        return "forward:/index";
    }

    /**
     * 处理/index请求，如果有请求参数会自动封装到page中
     */
    @GetMapping("/index")
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode", defaultValue = "0") int orderMode){
        //配置分页数据，总记录数和访问路径
        page.setRows(discussPostService.getDiscussPostRow(0));
        page.setPath("/index?orderMode=" + orderMode);

        List<DiscussPost> list = discussPostService.getDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);
        List<Map<String, Object>> discussPosts = new ArrayList<>();

        if(list != null) {
            for (DiscussPost discussPost : list) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                User user = userMapper.selectUserById(discussPost.getUserId());
                map.put("user", user);
                // 设置主页帖子赞的数量
                long likeCount = likeService.findLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }

        User user = hostHolder.getUser();
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode", orderMode);
        return "index";
    }

    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }

    @GetMapping("/denied")
    public String getDeniedPage() {
        return "/error/404";
    }

}
