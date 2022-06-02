package com.sadness.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

/**
 * @version 1.0
 * @Date 2022/5/30 10:19
 * @Author SadAndBeautiful
 */
@Configuration
public class AlphaConfig {

    @Bean
    public SimpleDateFormat simpleDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
