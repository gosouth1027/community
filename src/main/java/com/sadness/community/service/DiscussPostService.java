package com.sadness.community.service;

import com.sadness.community.dao.DiscussPostMapper;
import com.sadness.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @version 1.0
 * @Date 2022/6/1 10:36
 * @Author SadAndBeautiful
 */
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> getDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int getDiscussPostRow(int userId) {
        return discussPostMapper.selectCount(userId);
    }
}
