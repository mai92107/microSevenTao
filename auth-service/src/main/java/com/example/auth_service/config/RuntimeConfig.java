package com.example.auth_service.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuntimeConfig {


    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;


    @PostConstruct
    public void init() {
        System.out.println("Testing version 8.0");

        System.out.println("dbUrl:" + dbUrl);
        System.out.println("username:" + username);
        System.out.println("password:" + password);

    }

}
