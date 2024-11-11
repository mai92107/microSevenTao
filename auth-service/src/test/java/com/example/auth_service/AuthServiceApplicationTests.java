package com.example.auth_service;

import com.example.auth_service.exception.DuplicateAccountException;
import com.example.auth_service.exception.DuplicateEmailException;
import com.example.auth_service.exception.LoginErrorException;
import com.example.auth_service.exception.RequestEmptyException;
import com.example.auth_service.model.USER_ROLE;
import com.example.auth_service.model.dto.SignInRequest;
import com.example.auth_service.model.dto.SignUpRequest;
import com.example.auth_service.service.AuthenticationService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
@Transactional
class AuthServiceApplicationTests {

    @Autowired
    AuthenticationService authenticationService;

    @Test
    void verifyUserSuccess() {
        try {
            authenticationService.verifyUser(
                    new SignInRequest("boss", "boss"));
        } catch (LoginErrorException e) {
            log.error(e.getMessage());
        }
    }

    @Test
    void verifyUserError() {
        try {
            authenticationService.verifyUser(
                    new SignInRequest("boss", "bossaa"));
        } catch (LoginErrorException e) {
            log.error(e.getMessage());
        }
    }//帳號密碼錯誤

    @Test
    void signUpSuccess() {
        try {
            authenticationService.signUp(
                    new SignUpRequest(
                            "sss",
                            "boss@email"));
        } catch (DuplicateEmailException e) {
            log.error(e.getMessage());
        }
    }
    @Test
    void signUpError() {
        try {
            authenticationService.signUp(
                    new SignUpRequest(
                            "sss",
                            "boss@boss.com"));
        } catch (DuplicateEmailException e) {
            log.error(e.getMessage());
        }
        //信箱重複
    }
    @Test
    void updateAccountSuccess() {
        try {
            authenticationService.updateAccount(
                    3L,
                            "abc");
        } catch (DuplicateAccountException e) {
            log.error(e.getMessage());
        }
    }
    @Test
    void updateAccountError() {
        try {
            authenticationService.updateAccount(
                    3L,
                            "boss");
        } catch (DuplicateAccountException e) {
            log.error(e.getMessage());
        }
    }//重複account

    @Test
    void updateRoleSuccess() throws RequestEmptyException {
        authenticationService.updateRole(
                3L,
                USER_ROLE.ROLE_HOTELER);
    }
    @Test
    void updateRoleError() {
        try {
            authenticationService.updateRole(
                    3L,
                    null);
        } catch (RequestEmptyException e) {
            log.error(e.getMessage());
        }
    }//未提供修改角色

}
