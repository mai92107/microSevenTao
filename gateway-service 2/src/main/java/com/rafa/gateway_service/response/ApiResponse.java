package com.rafa.gateway_service.response;

import jdk.jfr.DataAmount;


public class ApiResponse<T> {
    private Integer status;     // 狀態 例如: 200, 400
    private String message; // 訊息 例如: 查詢成功, 新增成功, 請求錯誤
    private T data; 	    // payload 實際資料
    // 成功回應
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<T>(200, message, data);
    }
    // 失敗回應
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<T>(status, message, null);
    }

    public Integer getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}