package com.rafa.gateway_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckController.class);

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("Health check request received");
        try {
            System.out.println("Health check processing...");
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            System.err.println("Health check failed"+e.getMessage());
            return ResponseEntity.status(500).body("Error");
        } finally {
            System.out.println("Health check response sent");
        }
    }
}

