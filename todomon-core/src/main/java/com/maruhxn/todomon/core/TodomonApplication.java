package com.maruhxn.todomon.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.maruhxn.todomon.infra", "com.maruhxn.todomon.core"})
public class TodomonApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodomonApplication.class, args);
	}

}
