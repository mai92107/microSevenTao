package com.example.auth_service.exception;

public class DuplicateEmailException extends SignupErrorException{

    public DuplicateEmailException() {
        super("此信箱已註冊過");
    }
}
