package com.spring.ia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication()
public class IaApplication {

	static void main(String[] args) {
		SpringApplication.run(IaApplication.class, args);
	}
}
