package com.example.auth_service.exception;

public class UsernameErrorException extends LoginErrorException{
    public UsernameErrorException() {
        super("查無此帳號");
    }
}
