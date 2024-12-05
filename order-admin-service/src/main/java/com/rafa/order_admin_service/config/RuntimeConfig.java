package com.rafa.order_admin_service.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuntimeConfig {

    @Value("${spring.rabbitmq.addresses}")
    private String rabbitAddresses;

    @Value("${spring.rabbitmq.username}")
    private String rabbitUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitmqPassword;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;


    @PostConstruct
    public void init() {
        System.out.println("Testing version 1.0");

        System.out.println("Rabbit Connection Details:");
        System.out.println("Addresses: " + rabbitAddresses);
        System.out.println("username: " + rabbitUsername);
        System.out.println("password: " + rabbitUsername);

        System.out.println("Connecting to mysql:");
        System.out.println("url:" + dbUrl);
        System.out.println("username:" + dbUsername);
        System.out.println("password:" + dbPassword);

    }

}
