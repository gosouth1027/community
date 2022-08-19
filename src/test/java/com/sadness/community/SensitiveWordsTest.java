package com.sadness.community;

import com.sadness.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @version 1.0
 * @Date 2022/6/8 18:04
 * @Author SadAndBeautiful
 */
@SpringBootTest
public class SensitiveWordsTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void test() {
        String s = "一个苹#@果和一个香蕉，买一个菠@#萝还有十个哈密瓜";
        System.out.println(sensitiveFilter.filter(s));
    }
}
