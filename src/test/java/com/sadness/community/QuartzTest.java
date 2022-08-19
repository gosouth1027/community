package com.sadness.community;

import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @version 1.0
 * @Date 2022/6/24 18:06
 * @Author SadAndBeautiful
 */
@SpringBootTest
public class QuartzTest {

    @Autowired
    private Scheduler scheduler;

    @Test
    public void testDeleteJob() {
        try {
            scheduler.deleteJob(new JobKey("alphaJob", "alphaJobGroup"));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
