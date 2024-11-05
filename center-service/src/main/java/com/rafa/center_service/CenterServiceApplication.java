package com.rafa.center_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class CenterServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CenterServiceApplication.class, args);
	}

}
