package com.sadness.community.event;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.sadness.community.entity.DiscussPost;
import com.sadness.community.entity.Event;
import com.sadness.community.entity.Message;
import com.sadness.community.service.DiscussPostService;
import com.sadness.community.service.ElasticsearchService;
import com.sadness.community.service.MessageService;
import com.sadness.community.util.CommunityConstant;
import com.sadness.community.util.CommunityUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

/**
 * @version 1.0
 * @Date 2022/6/16 19:47
 * @Author SadAndBeautiful
 */
@Component
@Slf4j
public class EventConsumer implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private DiscussPostService discussPostService;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    /**
     * 点赞，关注，评论触发事件，消费者发送系统通知
     */
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord record) {
        // 判断读到的消息是否为空
        if (record == null || record.value() == null) {
            log.error("消息内容为空！");
            return;
        }

        // 读事件
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            log.error("消息格式不正确！");
            return;
        }

        // 创建Message
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        content.put("userId", event.getUserId());
        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    /**
     * 发帖，评论触发事件，消费者将帖子重新存到es中
     */
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        // 判断读到的消息是否为空
        if (record == null || record.value() == null) {
            log.error("消息内容为空！");
            return;
        }
        // 读事件
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            log.error("消息格式不正确！");
            return;
        }
        DiscussPost discussPost = discussPostService.findDiscussPost(event.getEntityId());
        elasticsearchService.saveDiscussPost(discussPost);
    }

    /**
     * 删帖触发事件，消费者将es中帖子删除
     */
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        // 判断读到的消息是否为空
        if (record == null || record.value() == null) {
            log.error("消息内容为空！");
            return;
        }
        // 读事件
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            log.error("消息格式不正确！");
            return;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    /**
     * Kafka执行wk命令生成长图
     */
    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord record) {
        // 判断读到的消息是否为空
        if (record == null || record.value() == null) {
            log.error("消息内容为空！");
            return;
        }
        // 读事件
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            log.error("消息格式不正确！");
            return;
        }

        // 取出event中的值
        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");
        // 拼接command命令
        String command = wkImageCommand + " --quality 75 " + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        // 执行命令
        try {
            Runtime.getRuntime().exec(command);
            log.info("生成长图成功！ " + command);
        } catch (IOException e) {
            log.error("生成长图失败！ " + e.getMessage());
        }

        // 启动定时器，监视图片是否已生成，一旦生成就上传到云服务器
        UploadTask task = new UploadTask(fileName, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 1000); // future可以停止定时器
        task.setFuture(future);

    }
    // 上传任务
    class UploadTask implements Runnable{

        // 文件名
        private String fileName;
        // 文件后缀
        private String suffix;
        // 执行任务的定时器的返回值
        private Future future;
        // 任务开始时间
        private long startTime;
        // 任务执行次数
        private int uploadTimes;

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        // 将图片上传到七牛云
        @Override
        public void run() {
            // 如果上传时间大于30秒，认为上传失败，终止任务
            if (System.currentTimeMillis() - startTime > 1000 * 60) {
                log.error("上传图片超时，任务终止: {}", fileName);
                future.cancel(true);
                return;
            }
            // 如果上传次数大于3次，认为上传失败，终止任务
            if (uploadTimes >= 3) {
                log.error("上传图片次数过多，任务终止: {}", fileName);
                future.cancel(true);
                return;
            }

            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            // 判断图像是否已经生成，若没生成等待生成，已生成开始上传
            if (!file.exists()) {
                log.info("等待图片生成：{}", fileName);
            } else {
                log.info("开始第{}次上传图片: {}.", ++uploadTimes, fileName);
                // 设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                // 生成上传凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                // 指定上传机房
                UploadManager manager = new UploadManager(new Configuration(Zone.zone1()));
                // 开始上传图片
                try {
                    // 上传图片
                    Response response = manager.put(path, fileName, uploadToken, null, "image" + suffix, false);
                    // 处理响应结果
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
                        log.warn("第{}次上传图片失败: {}.", uploadTimes, fileName);
                    } else {
                        log.info("第{}次上传图片成功: {}.", uploadTimes, fileName);
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    log.warn("第{}次上传图片失败: {}.", uploadTimes, fileName);
                }
            }
        }
    }
}
