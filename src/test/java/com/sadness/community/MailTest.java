package com.sadness.community;

import com.sadness.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * @version 1.0
 * @Date 2022/6/3 11:11
 * @Author SadAndBeautiful
 */
@SpringBootTest
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void sent() {
        mailClient.sentMail("656519184@qq.com", "hello", "hh");
    }

    @Test
    public void sentHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "燕燕");
        String process = templateEngine.process("/mail/demo1", context);
        mailClient.sentMail("656519184@qq.com", "summer", process);
    }
}
