package com.example.user_service.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuntimeConfig {


    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;


    @PostConstruct
    public void init() {
        System.out.println("Testing version 6.0");

        System.out.println("Redis Connection Details:");
        System.out.println("Host: " + redisHost);
        System.out.println("Port: " + port);

        System.out.println("Connecting to mysql:");
        System.out.println("url:" + dbUrl);
        System.out.println("username:" + dbUsername);
        System.out.println("password:" + dbPassword);

    }

}
