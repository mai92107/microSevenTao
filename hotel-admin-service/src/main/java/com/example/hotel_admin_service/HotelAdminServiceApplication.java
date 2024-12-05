package com.example.hotel_admin_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.TimeZone;

@SpringBootApplication
@EnableFeignClients
@EnableCaching
@EnableDiscoveryClient
public class HotelAdminServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(HotelAdminServiceApplication.class, args);
	}
}
