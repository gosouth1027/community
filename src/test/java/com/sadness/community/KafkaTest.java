package com.sadness.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @Date 2022/6/16 16:06
 * @Author SadAndBeautiful
 */
@SpringBootTest
public class KafkaTest {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void test() throws InterruptedException {
        kafkaProducer.sendMessage("test", "hello");
        kafkaProducer.sendMessage("test", "world");
        Thread.sleep(1000 * 6);
    }
}

@Component
class KafkaProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}

@Component
class KafkaConsumer {

    @KafkaListener(topics = {"test"})
    public void getMessage(ConsumerRecord record) {
        System.out.println(record.value());
    }
}
