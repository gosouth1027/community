package com.sadness.community;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Date;
import java.util.concurrent.*;

/**
 * @version 1.0
 * @Date 2022/6/24 15:48
 * @Author SadAndBeautiful
 */
@SpringBootTest
@Slf4j
public class ThreadPoolTest {

    // JDK自带的线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    // JDK可执行定时任务的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    // Spring普通的线程池
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    // Spring带定时任务的线程池
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    // 睡眠m毫秒
    private void sleep(Long m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试JDK自带的ExecutorService线程池
     */
    @Test
    public void testExecutorService() {
        // 线程启动的任务
        Runnable task = new Runnable() {
            @Override
            public void run() {
                log.debug("Hello ExecutorService");
            }
        };
        // 线程执行10次
        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }
        sleep(10000L);
    }

    /**
     * 测试JDK自带定时线程池ScheduledExecutorService
     */
    @Test
    public void testScheduledExecutorService() {
        // 线程启动的任务
        Runnable task = new Runnable() {
            @Override
            public void run() {
                log.debug("Hello ScheduledExecutorService");
            }
        };
        // 启动定时线程
        scheduledExecutorService.scheduleAtFixedRate(task, 5000, 2000, TimeUnit.MILLISECONDS);
        sleep(20000L);
    }

    /**
     * 测试Spring普通的线程池
     */
    @Test
    public void testThreadPoolTaskExecutor() {
        // 线程启动的任务
        Runnable task = new Runnable() {
            @Override
            public void run() {
                log.debug("Hello ScheduledExecutorService");
            }
        };
        for (int i = 0; i < 10; i++) {
            threadPoolTaskExecutor.submit(task);
        }
        sleep(10000L);
    }

    @Test
    public void testThreadPoolTaskScheduler() {
        // 线程启动的任务
        Runnable task = new Runnable() {
            @Override
            public void run() {
                log.debug("Hello ScheduledExecutorService");
            }
        };
        Date date = new Date(System.currentTimeMillis() + 10000);
        threadPoolTaskScheduler.scheduleAtFixedRate(task, date, 2000);
        sleep(20000L);
    }
}
