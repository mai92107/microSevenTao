package com.example.user_service.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignupErrorException extends Exception{
    String message;
}
