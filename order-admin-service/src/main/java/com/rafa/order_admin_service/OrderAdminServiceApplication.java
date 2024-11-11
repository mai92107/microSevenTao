package com.rafa.order_admin_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class OrderAdminServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderAdminServiceApplication.class, args);
	}

}
