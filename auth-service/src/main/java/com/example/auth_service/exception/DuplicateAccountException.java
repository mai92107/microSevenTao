package com.example.auth_service.exception;

public class DuplicateAccountException extends SignupErrorException{

    public DuplicateAccountException(String account) {
        super("此帳號已被使用");
    }
}
