package com.sadness.community;

import com.sadness.community.dao.LoginTicketMapper;
import com.sadness.community.entity.LoginTicket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

/**
 * @version 1.0
 * @Date 2022/6/6 16:18
 * @Author SadAndBeautiful
 */
@SpringBootTest
public class LoginTicketMapperTest {
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void test1() {
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(111);
        ticket.setTicket("asd");
        ticket.setStatus(0);
        ticket.setExpired(new Date(System.currentTimeMillis() + 60*1000*10));

        loginTicketMapper.insertLoginTicket(ticket);

        System.out.println(loginTicketMapper.selectLoginTicket("asd"));

        loginTicketMapper.updateLoginTicket("asd", 1);
    }
}
