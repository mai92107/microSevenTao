package com.example.user_service.exception;

public class DuplicateEmailException extends SignupErrorException{

    public DuplicateEmailException(String email) {
        super("此信箱已註冊過："+email);
    }
}
