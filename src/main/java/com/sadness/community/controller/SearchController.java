package com.sadness.community.controller;

import com.sadness.community.entity.DiscussPost;
import com.sadness.community.entity.Page;
import com.sadness.community.service.ElasticsearchService;
import com.sadness.community.service.LikeService;
import com.sadness.community.service.UserService;
import com.sadness.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2022/6/20 16:04
 * @Author SadAndBeautiful
 */
@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    /**
     * 响应搜索页面，处理搜索请求
     */
    @GetMapping("/search")
    public String search(String keyword, Page page, Model model) throws IOException {

        // 根据关键词查询结果
        Map<String, Object> map = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        // 处理查询结果
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        long total = 0;
        if (map != null) {
            total = (long) map.get("total");
            List<DiscussPost> list = (List<DiscussPost>) map.get("discussPosts");

            for (DiscussPost post : list) {
                Map<String, Object> postMap = new HashMap<>();
                // 帖子
                postMap.put("post", post);
                // 帖子作者
                postMap.put("user", userService.getUserById(post.getUserId()));
                // 帖子点赞数量
                postMap.put("likeCount", likeService.findLikeCount(ENTITY_TYPE_POST, post.getId()));

                discussPosts.add(postMap);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        page.setPath("/search?keyword=" + keyword);
        page.setRows((int) total);

        return "site/search";
    }
}
