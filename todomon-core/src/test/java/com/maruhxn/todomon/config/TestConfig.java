package com.maruhxn.todomon.config;

import com.maruhxn.todomon.core.util.TestTodoFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public TestTodoFactory testTodoFactory() {
        return new TestTodoFactory();
    }
}
