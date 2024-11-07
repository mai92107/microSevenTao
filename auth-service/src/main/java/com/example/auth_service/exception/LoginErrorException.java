package com.example.auth_service.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginErrorException extends Exception{
    String message;
}
