package com.example.user_service.model.dto;

import lombok.Data;

@Data
public class SignInRequest {
    String userName;
    String passWord;
}
