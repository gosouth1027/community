package com.sadness.community.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @version 1.0
 * @Date 2022/6/25 16:45
 * @Author SadAndBeautiful
 */
@Configuration
@Slf4j
public class WkConfig {

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    /**
     * 判断图片存储路径是否存在，不存在则创建
     */
    @PostConstruct
    public void init() {
        File file = new File(wkImageStorage);
        if (!file.exists()) {
            file.mkdirs();
            log.info("创建图片存储目录：" + wkImageStorage);
        }
    }
}
