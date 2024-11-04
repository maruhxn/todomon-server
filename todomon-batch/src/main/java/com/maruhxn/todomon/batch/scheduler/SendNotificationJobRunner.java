package com.maruhxn.todomon.batch.scheduler;

import com.maruhxn.todomon.batch.scheduler.job.SendNotificationSchJob;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class SendNotificationJobRunner extends JobRunner {

    @Autowired
    private Scheduler scheduler;

    @Override
    protected void doRun(ApplicationArguments args) {
        JobDetail jobDetail = buildJobDetail(SendNotificationSchJob.class, "sendNotificationJob", "batch", new HashMap());

        // 스케줄러 시간 정보
        Trigger trigger = buildJobTrigger("0/10 * * * * ?"); // 10초마다 실행

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
