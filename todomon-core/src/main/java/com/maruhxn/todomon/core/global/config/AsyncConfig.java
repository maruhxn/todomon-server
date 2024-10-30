package com.maruhxn.todomon.core.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public Executor asyncThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int numOfCores = Runtime.getRuntime().availableProcessors();
        float targetCpuUtilization = 0.3f;
        float blockingCoefficient = 0.2f;
        int corePoolSize = (int) (numOfCores * targetCpuUtilization * (1 + blockingCoefficient));
        executor.setCorePoolSize(corePoolSize);
        executor.setThreadNamePrefix("TodomonAsyncThread - ");
        executor.initialize();
        return executor;
    }
}
