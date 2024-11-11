package com.example.auth_service.service;


import com.example.auth_service.exception.*;
import com.example.auth_service.model.USER_ROLE;
import com.example.auth_service.model.Users;
import com.example.auth_service.model.dto.LoginResponse;
import com.example.auth_service.model.dto.SignInRequest;
import com.example.auth_service.model.dto.SignUpRequest;


public interface AuthenticationService {

    public LoginResponse verifyUser(SignInRequest request) throws LoginErrorException;

    public LoginResponse signUp(SignUpRequest request)throws DuplicateEmailException;

    public void updateAccount(Long userId , String account) throws DuplicateAccountException;

    public void updateRole(Long userId, USER_ROLE role) throws RequestEmptyException;
}
