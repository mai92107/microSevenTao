package com.rafa.comment_service.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuntimeConfig {

    @Value("${spring.rabbitmq.host}")
    private String rabbitHost;



    @PostConstruct
    public void init() {
        System.out.println("Testing version 1.0");

        System.out.println("Socket Connection Details:");
        System.out.println("host: " + rabbitHost);

    }

}
