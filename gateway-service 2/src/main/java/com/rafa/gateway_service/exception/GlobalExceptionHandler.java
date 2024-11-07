package com.rafa.gateway_service.exception;

import com.rafa.gateway_service.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpServerErrorException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpServerErrorException.GatewayTimeout.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ResponseEntity<ApiResponse<Object>> handleServiceConnectOutOfTime(HttpServerErrorException e){
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.GATEWAY_TIMEOUT.value(), "連線逾時，請檢查server端");
        return new ResponseEntity<>(response, HttpStatusCode.valueOf((response.getStatus())));
    }
}
