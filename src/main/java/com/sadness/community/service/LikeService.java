package com.sadness.community.service;

import com.sadness.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @Date 2022/6/14 11:31
 * @Author SadAndBeautiful
 */
@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞
     */
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        // 使用事务管理
       redisTemplate.execute(new SessionCallback() {
           @Override
           public Object execute(RedisOperations operations) throws DataAccessException {
               String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
               String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
               // 查询点赞状态
               Boolean isLike = operations.opsForSet().isMember(entityLikeKey, userId);
               // 开启事务
               redisTemplate.multi();
               if (isLike) {
                   operations.opsForSet().remove(entityLikeKey, userId);
                   operations.opsForValue().decrement(userLikeKey);
               } else {
                   operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
               }
               return redisTemplate.exec();
           }
       });
    }

    /**
     * 查询帖子的点赞数量
     */
    public long findLikeCount(int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(likeKey);
    }

    /**
     * 查询当前用户点赞状态
     */
    public int findUserLikeStatus(int userId, int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(likeKey, userId) ? 1 : 0;
    }

    /**
     * 查询用户被赞数量
     */
    public int findUserLikedCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer userLikedCount = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return userLikedCount == null ? 0 : userLikedCount.intValue();
    }

}
