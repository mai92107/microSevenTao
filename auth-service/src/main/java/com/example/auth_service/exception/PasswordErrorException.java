package com.example.auth_service.exception;

public class PasswordErrorException extends LoginErrorException {
    public PasswordErrorException() {
        super("密碼錯誤，請重新輸入");
    }
}
