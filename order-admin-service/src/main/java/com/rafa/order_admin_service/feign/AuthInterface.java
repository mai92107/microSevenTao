package com.rafa.order_admin_service.feign;

import com.rafa.order_admin_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service",url = "http://auth-service.seventao:8080", fallback = AuthServiceFallback.class)//加入要映射的名稱（全大寫）
public interface AuthInterface {
    //加入要映射的方法

    @GetMapping("/auth-service/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateJwt(@RequestHeader("Authorization") String jwt);

    @GetMapping("/auth-service/findUser")
    public ResponseEntity<ApiResponse<Long>> findUserIdByJwt(@RequestHeader("Authorization") String jwt);

}

@Component
class AuthServiceFallback implements AuthInterface {
    @Override
    public ResponseEntity<ApiResponse<Boolean>> validateJwt(String jwt) {
        System.out.println("(AuthInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }

    @Override
    public ResponseEntity<ApiResponse<Long>> findUserIdByJwt(String jwt) {
        System.out.println("(AuthInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }
}
