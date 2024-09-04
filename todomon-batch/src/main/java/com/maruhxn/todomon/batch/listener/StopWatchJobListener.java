package com.maruhxn.todomon.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.time.Duration;

@Slf4j
public class StopWatchJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Duration diff = Duration.between(jobExecution.getStartTime().toLocalTime(), jobExecution.getEndTime().toLocalTime());
        long time = diff.toMillis();
        log.info("========================================");
        log.info("총 소요 시간 : {}", time);
        log.info("========================================");
    }
}
