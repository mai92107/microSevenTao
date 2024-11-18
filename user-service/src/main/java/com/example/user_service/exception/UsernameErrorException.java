package com.example.user_service.exception;

public class UsernameErrorException extends LoginErrorException{
    public UsernameErrorException(String username) {
        super("查無此帳號："+username);
    }
}
