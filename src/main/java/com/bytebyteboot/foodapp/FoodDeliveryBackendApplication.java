package com.bytebyteboot.foodapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@SpringBootApplication
@EnableAsync
public class FoodDeliveryBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodDeliveryBackendApplication.class, args);
		log.info("FoodApp application started successfully!");
	}

}
