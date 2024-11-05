package com.rafa.hotel_service.feign.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient("AUTH-SERVICE")//加入要映射的名稱（全大寫）
public interface AuthInterface {
    //加入要映射的方法

    @GetMapping("/auth/findUser")
    public ResponseEntity<Long> findUserIdByJwt(@RequestHeader("Authorization") String jwt);

}
