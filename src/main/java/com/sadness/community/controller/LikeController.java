package com.sadness.community.controller;

import com.sadness.community.annotation.LoginRequired;
import com.sadness.community.entity.Event;
import com.sadness.community.entity.User;
import com.sadness.community.event.EventProducer;
import com.sadness.community.service.LikeService;
import com.sadness.community.util.CommunityConstant;
import com.sadness.community.util.CommunityUtil;
import com.sadness.community.util.HostHolder;
import com.sadness.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2022/6/14 11:46
 * @Author SadAndBeautiful
 */
@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @LoginRequired
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {

        User user = hostHolder.getUser();
        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 帖子点赞数量
        long likeCount = likeService.findLikeCount(entityType, entityId);
        // 用户点赞状态
        int likeStatus = likeService.findUserLikeStatus(user.getId(), entityType, entityId);

        // 处理点赞事件，只有在点赞的时候才触发
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);

            eventProducer.fireEvent(event);
        }

        // 如果是对帖子进行点赞
        if (entityType == ENTITY_TYPE_POST) {
            // 将贴子存入redis等待计算分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);
        return CommunityUtil.getJSONString(0, null, map);
    }
}
