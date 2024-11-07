package com.example.user_service.exception;

import com.example.user_service.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LoginErrorException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Object>> handleLoginError(LoginErrorException e){
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf((response.getStatus())));
    }

    @ExceptionHandler(SignupErrorException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Object>> handleSignupError(SignupErrorException e){
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf((response.getStatus())));
    }


    @ExceptionHandler(RequestEmptyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<com.example.user_service.response.ApiResponse<Object>> handleNoRequestException(RequestEmptyException e){
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf((response.getStatus())));
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<com.example.user_service.response.ApiResponse<Object>> handleNoRequestException(InvalidTokenException e){
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf((response.getStatus())));
    }
}
