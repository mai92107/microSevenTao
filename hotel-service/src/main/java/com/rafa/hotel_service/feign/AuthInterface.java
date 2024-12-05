package com.rafa.hotel_service.feign;

import com.rafa.hotel_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "auth-service",url = "http://auth-service.seventao:8080",fallback = AuthServiceFallback.class)//加入要映射的名稱（全大寫）
public interface AuthInterface {
    //加入要映射的方法
    @GetMapping("/auth-service/findUser")
    public ResponseEntity<ApiResponse<Long>> findUserIdByJwt(@RequestHeader("Authorization") String jwt);
}

@Component
class AuthServiceFallback implements AuthInterface {
    @Override
    public ResponseEntity<ApiResponse<Long>> findUserIdByJwt(String jwt) {
        System.out.println("(AuthInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }
}
