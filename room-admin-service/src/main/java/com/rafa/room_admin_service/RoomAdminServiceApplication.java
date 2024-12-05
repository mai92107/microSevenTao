package com.rafa.room_admin_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableCaching
@EnableDiscoveryClient
public class RoomAdminServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomAdminServiceApplication.class, args);
	}

}
