package com.sadness.community.event;

import com.alibaba.fastjson.JSONObject;
import com.sadness.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @Date 2022/6/16 19:44
 * @Author SadAndBeautiful
 */
@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 处理事件
     */
    public void fireEvent(Event event) {
        // 将事件发送到指定主题
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
