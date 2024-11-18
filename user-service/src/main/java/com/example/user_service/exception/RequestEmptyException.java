package com.example.user_service.exception;

public class RequestEmptyException extends UserException{

    public RequestEmptyException() {
        super("未接收到資料");
    }
}
