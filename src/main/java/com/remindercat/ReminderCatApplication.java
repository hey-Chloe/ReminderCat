package com.remindercat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ReminderCatApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReminderCatApplication.class, args);
	}

}
