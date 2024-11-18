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
import org.springframework.web.bind.annotation.*;

@FeignClient("AUTH-SERVICE")//加入要映射的名稱（全大寫）
public interface AuthInterface {
    //加入要映射的方法

    @GetMapping("/auth/findUser")
    public ResponseEntity<ApiResponse<Long>> findUserIdByJwt(@RequestHeader("Authorization") String jwt);

    @PostMapping("/auth/signUp")
    public ResponseEntity<ApiResponse<LoginResponse>> signUp(@RequestBody SignUpRequest request);

    @PostMapping("/auth/signIn")
    public ResponseEntity<ApiResponse<LoginResponse>> signIn(@RequestBody SignInRequest request);

    @PutMapping("/auth/member/account")
    public ResponseEntity<ApiResponse<String>> updateAccount(@RequestHeader("Authorization") String jwt, @RequestBody String account);

    @PutMapping("/auth/member/role")
    public ResponseEntity<ApiResponse<String>> updateRole(@RequestHeader("Authorization") String jwt, @RequestBody USER_ROLE role);
}
