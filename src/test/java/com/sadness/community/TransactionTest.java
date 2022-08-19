package com.sadness.community;

import com.sadness.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @version 1.0
 * @Date 2022/6/9 22:04
 * @Author SadAndBeautiful
 */
@SpringBootTest
public class TransactionTest {

    @Autowired
    private AlphaService alphaService;

    @Test
    public void test1() {
        alphaService.transactionTwo();
    }

}
