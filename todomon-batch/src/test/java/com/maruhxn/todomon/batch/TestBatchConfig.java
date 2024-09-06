package com.maruhxn.todomon.batch;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Profile("test")
@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = "com.maruhxn.todomon") // 엔티티가 위치한 패키지
@EnableJpaRepositories(basePackages = "com.maruhxn.todomon")
public class TestBatchConfig {

}
