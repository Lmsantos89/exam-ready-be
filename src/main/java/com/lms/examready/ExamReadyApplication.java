package com.lms.examready;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
public class ExamReadyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExamReadyApplication.class, args);
	}

}
