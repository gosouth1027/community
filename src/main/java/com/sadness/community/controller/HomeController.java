package com.sadness.community.controller;

import com.sadness.community.dao.DiscussPostMapper;
import com.sadness.community.dao.UserMapper;
import com.sadness.community.entity.DiscussPost;
import com.sadness.community.entity.Page;
import com.sadness.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
public class HomeController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    /**
     * 处理/index请求，如果有请求参数会自动封装到page中
     */
    @GetMapping("/index")
    public String getIndexPage(Model model, Page page){
        //配置分页数据，总记录数和访问路径
        page.setRows(discussPostMapper.selectCount(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();

        if(list != null) {
            for (DiscussPost discussPost : list) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                User user = userMapper.selectUserById(discussPost.getUserId());
                map.put("user", user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "index";
    }
}
