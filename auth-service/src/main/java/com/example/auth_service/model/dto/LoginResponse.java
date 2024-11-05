package com.example.auth_service.model.dto;

import com.example.auth_service.model.USER_ROLE;
import lombok.Data;

@Data
public class LoginResponse {
    private Long userId;
    private String jwt;
    private USER_ROLE role;
    private String username;
}
