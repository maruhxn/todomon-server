package com.maruhxn.todomon.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SimpleJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job job() {
        return new JobBuilder("job", jobRepository)
                .start(step())
                .build();
    }

    @Bean
    public Step step() {
        return new StepBuilder("step", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("step was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }
}
