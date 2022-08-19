package com.sadness.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sadness.community.dao.DiscussPostMapper;
import com.sadness.community.entity.DiscussPost;
import com.sadness.community.util.SensitiveFilter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @Date 2022/6/1 10:36
 * @Author SadAndBeautiful
 */
@Service
@Slf4j
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.post.max-size}")
    private int maxSize;

    @Value("${caffeine.post.expire-seconds}")
    private int expireSeconds;

    // 帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子数量缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    /**
     * 初始换两个Caffeine缓存
     */
    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    // 缓存中没有数据查询数据的方法
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key.length() == 0) {
                            throw new IllegalArgumentException("参数错误！");
                        }
                        String[] keys = key.split(":");
                        if (keys.length != 2) {
                            throw new IllegalArgumentException("参数错误！");
                        }
                        int offset = Integer.parseInt(keys[0]);
                        int limit = Integer.parseInt(keys[1]);

                        // 二级缓存：Redis
                        log.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });
        // 初始化帖子数量缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {
                        log.debug("load post rows from DB.");
                        return discussPostMapper.selectCount(key);
                    }
                });
    }


    /**
     * 查询帖子
     */
    public List<DiscussPost> getDiscussPosts(int userId, int offset, int limit, int orderMode) {
        // 当查找热门帖子时，先从Caffeine缓存中取
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }
        log.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    /**
     * 查询帖子数量
     */
    public int getDiscussPostRow(int userId) {
        if (userId == 0) {
            return postRowsCache.get(userId);
        }
        log.debug("load post rows from DB.");
        return discussPostMapper.selectCount(userId);
    }

    /**
     * 增加帖子，转义html标记，过滤敏感词
     */
    public int addDiscussPost(DiscussPost post) {
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    /**
     * 根据id查询贴子
     */
    public DiscussPost findDiscussPost(int id) {
        return discussPostMapper.getDiscussPostById(id);
    }

    /**
     * 更新帖子评论数量
     */
    public int updateCommentCount(int commentId, int count) {
        return discussPostMapper.updateCommentCount(commentId, count);
    }

    /**
     * 修改帖子类型  0：普通，1：置顶
     */
    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    /**
     * 修改帖子状态  0：正常 1：加精 2：拉黑
     */
    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    /**
     * 更新帖子分数
     */
    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }

}
