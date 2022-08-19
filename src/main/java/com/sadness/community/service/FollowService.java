package com.sadness.community.service;

import com.sadness.community.entity.User;
import com.sadness.community.util.CommunityConstant;
import com.sadness.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @version 1.0
 * @Date 2022/6/14 21:45
 * @Author SadAndBeautiful
 */

@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * 关注
     */
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 获取键
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                // 开启事务
                redisTemplate.multi();
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return redisTemplate.exec();
            }
        });
    }

    /**
     * 取关
     */
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 获取键
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                // 开启事务
                redisTemplate.multi();

                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);

                return redisTemplate.exec();
            }
        });
    }

    /**
     * 查询用户关注实体的数量
     */
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * 查询实体的粉丝数量
     */
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     * 查询用户是否已关注某实体
     */
    public boolean isFollow(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    /**
     * 查询用户的关注列表，支持分页
     */
    public List<Map<String, Object>> findUserFollowee(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        // 查询用户关注列表
        Set<Integer> followeeIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        List<Map<String, Object>> followeeList = new ArrayList<>();
        if (followeeIds == null) {
            return null;
        } else {
            for (Integer followeeId : followeeIds) {
                Map<String, Object> map = new HashMap<>();
                // 存入关注人的信息
                User followee = userService.getUserById(followeeId);
                map.put("followee", followee);
                // 关注的时间
                Double score = redisTemplate.opsForZSet().score(followeeKey, followeeId);
                map.put("followTime", new Date(score.longValue()));
                followeeList.add(map);
            }
        }
        return followeeList;
    }

    /**
     * 查询用户的粉丝列表，支持分页
     */
    public List<Map<String, Object>> findUserFollower(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        // 查询用户粉丝列表
        Set<Integer> followerIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        List<Map<String, Object>> followerList = new ArrayList<>();
        if (followerIds == null) {
            return null;
        } else {
            for (Integer followerId : followerIds) {
                Map<String, Object> map = new HashMap<>();
                // 存入关注人的信息
                User follower = userService.getUserById(followerId);
                map.put("follower", follower);
                // 关注的时间
                Double score = redisTemplate.opsForZSet().score(followerKey, followerId);
                map.put("followTime", new Date(score.longValue()));
                followerList.add(map);
            }
        }
        return followerList;
    }
}
