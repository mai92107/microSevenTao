package com.rafa.room_admin_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RoomAdminServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomAdminServiceApplication.class, args);
	}

}