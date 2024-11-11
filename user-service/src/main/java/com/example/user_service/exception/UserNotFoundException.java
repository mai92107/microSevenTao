package com.example.user_service.exception;

public class UserNotFoundException extends UserException{
    public UserNotFoundException(Long userId) {
        super("無法找到此使用者資料"+userId);
    }
}
