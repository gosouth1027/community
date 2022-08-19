package com.sadness.community;

import com.sadness.community.dao.MessageMapper;
import com.sadness.community.entity.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @version 1.0
 * @Date 2022/6/12 16:26
 * @Author SadAndBeautiful
 */
@SpringBootTest
public class MessageTest {

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void test() {
//        List<Message> messages = messageMapper.selectConversationsByUserId(111, 0, 20);
//        for (Message message : messages) {
//            System.out.println(message);
//        }
//        System.out.println(messageMapper.selectConversationCountByUserId(111));

        List<Message> messages = messageMapper.selectLettersByConversation("111_112", 0, 20);
        for (Message message : messages) {
            System.out.println(message);
        }

        System.out.println(messageMapper.selectLettersCountByConversation("111_112"));

        System.out.println(messageMapper.selectLetterUnreadCount(111, null));

    }
}
