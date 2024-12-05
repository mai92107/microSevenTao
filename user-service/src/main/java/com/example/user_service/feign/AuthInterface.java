package com.example.user_service.feign;


import com.example.user_service.exception.InvalidTokenException;
import com.example.user_service.exception.LoginErrorException;
import com.example.user_service.exception.RequestEmptyException;
import com.example.user_service.exception.SignupErrorException;
import com.example.user_service.model.USER_ROLE;
import com.example.user_service.model.dto.LoginResponse;
import com.example.user_service.model.dto.SignInRequest;
import com.example.user_service.model.dto.SignUpRequest;
import com.example.user_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "auth-service",url = "http://auth-service.seventao:8080",fallback = AuthServiceFallback.class)//加入要映射的名稱（全大寫）
public interface AuthInterface {
    //加入要映射的方法

    @GetMapping("/auth-service/findUser")
    public ResponseEntity<ApiResponse<Long>> findUserIdByJwt(@RequestHeader("Authorization") String jwt);

    @PostMapping("/auth-service/signUp")
    public ResponseEntity<ApiResponse<LoginResponse>> signUp(@RequestBody SignUpRequest request);

    @PostMapping("/auth-service/signIn")
    public ResponseEntity<ApiResponse<LoginResponse>> signIn(@RequestBody SignInRequest request);

    @PutMapping("/auth-service/member/account")
    public ResponseEntity<ApiResponse<String>> updateAccount(@RequestHeader("Authorization") String jwt, @RequestBody String account);

    @PutMapping("/auth-service/member/role")
    public ResponseEntity<ApiResponse<String>> updateRole(@RequestHeader("Authorization") String jwt, @RequestBody USER_ROLE role);
}

@Component
class AuthServiceFallback implements AuthInterface {

    @Override
    public ResponseEntity<ApiResponse<Long>> findUserIdByJwt(String jwt) {

        System.out.println("(AuthInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }

    @Override
    public ResponseEntity<ApiResponse<LoginResponse>> signUp(SignUpRequest request) {

        System.out.println("(AuthInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }

    @Override
    public ResponseEntity<ApiResponse<LoginResponse>> signIn(SignInRequest request) {

        System.out.println("(AuthInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }

    @Override
    public ResponseEntity<ApiResponse<String>> updateAccount(String jwt, String account) {

        System.out.println("(AuthInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }

    @Override
    public ResponseEntity<ApiResponse<String>> updateRole(String jwt, USER_ROLE role) {

        System.out.println("(AuthInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }
}
