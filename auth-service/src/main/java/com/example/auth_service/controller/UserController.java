package com.example.auth_service.controller;


import com.example.auth_service.exception.*;
import com.example.auth_service.model.USER_ROLE;
import com.example.auth_service.model.Users;
import com.example.auth_service.model.dto.LoginResponse;
import com.example.auth_service.model.dto.SignInRequest;
import com.example.auth_service.model.dto.SignUpRequest;
import com.example.auth_service.response.ApiResponse;
import com.example.auth_service.service.AuthenticationService;
import com.example.auth_service.service.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    Environment environment;

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateJwt(@RequestHeader("Authorization") String jwt) {
        if (jwt.isEmpty())
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "查無此人"));
        boolean isSuccess = jwtProvider.validateJwt(jwt);
        return ResponseEntity.ok(ApiResponse.success("查詢成功", isSuccess));

    }

    @GetMapping("/findUser")
    public ResponseEntity<ApiResponse<Long>> findUserIdByJwt(@RequestHeader("Authorization") String jwt) {
        if (jwt.isEmpty())
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "查無此人"));
        if (!jwtProvider.validateJwt(jwt))
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "查無此人"));
        Long userId = jwtProvider.findUserIdByJwt(jwt);
        log.info("這是使用者"+userId);
        return ResponseEntity.ok(ApiResponse.success("查詢成功", userId));

    }


    @PostMapping("/signIn")
    public ResponseEntity<ApiResponse<LoginResponse>> signIn(@RequestBody SignInRequest request) {
        try {
            System.out.println("我要登入" + request.getUserName());
            LoginResponse res = authenticationService.verifyUser(request);
            System.out.println("取得資料");
            return ResponseEntity.ok(ApiResponse.success("成功登入", res));
        } catch (LoginErrorException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PostMapping("/signUp")
    public ResponseEntity<ApiResponse<LoginResponse>> signUp(@RequestBody SignUpRequest request) {
        System.out.println(request);
        if (request == null)
            return ResponseEntity.ok().body(ApiResponse.error(400, "註冊資料不可空白"));
        try {
            LoginResponse res = authenticationService.signUp(request);
            return ResponseEntity.ok(ApiResponse.success("成功註冊", res));
        } catch (SignupErrorException e) {
            return ResponseEntity.ok().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/member/account")
    public ResponseEntity<ApiResponse<String>> updateAccount(@RequestHeader("Authorization") String jwt, @RequestBody String account) {

        System.out.println("我是" + account);
        Long userId = jwtProvider.findUserIdByJwt(jwt);
        System.out.println("我的userID" + userId);
        try {
            authenticationService.updateAccount(userId, account);
            return ResponseEntity.ok(ApiResponse.success("修改成功", null));
        } catch (DuplicateAccountException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/member/role")
    public ResponseEntity<ApiResponse<String>> updateRole(@RequestHeader("Authorization") String jwt, @RequestBody USER_ROLE role) {

        Long userId = jwtProvider.findUserIdByJwt(jwt);
        System.out.println("我的userID" + userId);

        try {
            authenticationService.updateRole(userId, role);
        } catch (RequestEmptyException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        System.out.println("認證中心修改完成");

        return ResponseEntity.ok(ApiResponse.success("修改成功", null));

    }
}
