package com.sadness.community.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @version 1.0
 * @Date 2022/6/24 17:41
 * @Author SadAndBeautiful
 */
public class AlphaJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println(Thread.currentThread().getName() + " : execute a quartz job.");
    }
}
