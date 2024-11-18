package com.rafa.order_service.feign;


import com.rafa.order_service.model.dto.UserDto;
import com.rafa.order_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("USER-SERVICE")//加入要映射的名稱（全大寫）
public interface UserInterface {
    //加入要映射的方法
    @GetMapping("/member")
    public ResponseEntity<ApiResponse<UserDto>> getUserProfile(@RequestHeader("Authorization") String jwt);

}
