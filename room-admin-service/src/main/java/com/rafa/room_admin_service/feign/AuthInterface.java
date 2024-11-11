package com.rafa.room_admin_service.feign;


import com.rafa.room_admin_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient("AUTH-SERVICE")//加入要映射的名稱（全大寫）
public interface AuthInterface {
    //加入要映射的方法

    @GetMapping("/auth/validate")
    public ResponseEntity<ApiResponse<Object>> validateJwt(@RequestHeader("Authorization") String jwt);

    @GetMapping("/auth/findUser")
    public ResponseEntity<ApiResponse<Object>> findUserIdByJwt(@RequestHeader("Authorization") String jwt);


}
