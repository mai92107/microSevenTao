package com.example.auth_service.model.dto;

import lombok.Data;

@Data
public class SignInRequest {
    String userName;
    String passWord;
}
