package com.rafa.comment_service.feign;

import com.rafa.comment_service.model.dto.UserDto;
import com.rafa.comment_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service",url = "http://user-service.seventao:8080",fallback = UserServiceFallback.class)//加入要映射的名稱（全大寫）
public interface UserInterface {
    //加入要映射的方法

    @GetMapping("/user-service/member")
    public ResponseEntity<ApiResponse<UserDto>> getUserProfile(@RequestHeader("Authorization") String jwt);
}

@Component
class UserServiceFallback implements UserInterface{

    @Override
    public ResponseEntity<ApiResponse<UserDto>> getUserProfile(String jwt) {
        System.out.println("(UserInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }
}
