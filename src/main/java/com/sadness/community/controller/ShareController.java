package com.sadness.community.controller;

import com.sadness.community.entity.Event;
import com.sadness.community.event.EventProducer;
import com.sadness.community.util.CommunityConstant;
import com.sadness.community.util.CommunityUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2022/6/26 9:17
 * @Author SadAndBeautiful
 */
@Controller
@Slf4j
public class ShareController implements CommunityConstant {

    @Autowired
    private EventProducer eventProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;

    /**
     * 根据htmlUrl生成长图
     */
    @GetMapping("/share")
    @ResponseBody
    public String share(String htmlUrl) {

        // 生成长图文件名
        String fileName = CommunityUtil.generateUUID();
        // 异步生成长图，通过事件触发，带生成长图的网站，文件名，后缀
        Event event = new Event()
                .setTopic(TOPIC_SHARE)
                .setData("htmlUrl", htmlUrl)
                .setData("fileName", fileName)
                .setData("suffix", ".png");
        eventProducer.fireEvent(event);

        // 返回查看长图的路径
        Map<String, Object> map = new HashMap<>();
//        map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);
        map.put("shareUrl", shareBucketUrl + "/" + fileName);
        return CommunityUtil.getJSONString(0, "生成长图成功，可通过后边链接查看！", map);
    }

    /**
     * 查看生成的长图
     */
    @GetMapping("/share/image/{fileName}")
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {

        // 判断文件名是否为空
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空！");
        }
        // 通过IO流来读写长图
        response.setContentType("image/png");
        File file = new File(wkImageStorage + "/" + fileName + ".png");
        try {
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] buffers = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffers)) != -1) {
                os.write(buffers, 0, b);
            }
        } catch (IOException e) {
            log.error("获取长图失败!原因:" + e.getMessage());
        }
    }
}
