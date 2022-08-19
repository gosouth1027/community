package com.sadness.community.quartz;

import com.sadness.community.entity.DiscussPost;
import com.sadness.community.service.DiscussPostService;
import com.sadness.community.service.ElasticsearchService;
import com.sadness.community.service.LikeService;
import com.sadness.community.util.CommunityConstant;
import com.sadness.community.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @version 1.0
 * @Date 2022/6/24 21:58
 * @Author SadAndBeautiful
 */
@Slf4j
public class PostScoreRefreshJob implements Job, CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    // 纪元
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("纪元初始化失败！", e);
        }
    }

    /**
     * 执行任务，刷新帖子分数
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 获取redis的key
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        if (operations.size() == 0) {
            log.info("[任务取消] 当前没有需要刷新的帖子");
            return;
        }
        log.info("[任务开始] 正在刷新贴子分数，需要刷新的帖子的数量：" + operations.size());
        while (operations.size() > 0) {
            refresh((Integer) operations.pop());
        }
        log.info("[任务完成] 帖子分数刷新完毕");
    }

    private void refresh(int postId) {

        DiscussPost post = discussPostService.findDiscussPost(postId);
        if (post == null) {
            log.info("待刷新的帖子不存在" + postId);
            return;
        }
        // 帖子是否加精
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findLikeCount(ENTITY_TYPE_POST, postId);
        // 计算帖子分数，并更新
        double weight = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        double score = Math.log10(Math.max(weight, 1)) + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新数据库以及es
        discussPostService.updateScore(postId, score);
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
