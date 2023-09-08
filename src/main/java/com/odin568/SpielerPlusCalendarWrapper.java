package com.odin568;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpielerPlusCalendarWrapper {

	public static void main(String[] args) {
		SpringApplication.run(SpielerPlusCalendarWrapper.class, args);
	}

}
