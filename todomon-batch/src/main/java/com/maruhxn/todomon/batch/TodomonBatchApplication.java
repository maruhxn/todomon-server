package com.maruhxn.todomon.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.maruhxn.todomon") // 엔티티가 위치한 패키지
@EnableJpaRepositories(basePackages = "com.maruhxn.todomon")
@ComponentScan(basePackages = {"com.maruhxn.todomon.infra", "com.maruhxn.todomon.batch"})
public class TodomonBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(TodomonBatchApplication.class, args);
    }
}
