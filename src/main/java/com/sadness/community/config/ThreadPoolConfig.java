package com.sadness.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @version 1.0
 * @Date 2022/6/24 16:33
 * @Author SadAndBeautiful
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
