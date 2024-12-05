package com.rafa.gateway_service.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuntimeConfig {


    @Value("${spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedOrigins}")
    private String frontendUrl;


    @PostConstruct
    public void init() {
        System.out.println("Testing version 6.0");

        System.out.println("frontendUrl:" + frontendUrl);

    }

}
