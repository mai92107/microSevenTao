package com.rafa.room_admin_service.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("HOTEL-ADMIN-SERVICE")//加入要映射的名稱（全大寫）
public interface HotelInterface {
    //加入要映射的方法

    @GetMapping("/boss/findBoss/{hotelId}")
    public ResponseEntity<Boolean> checkIsBoss(@RequestHeader("Authorization") String jwt,@PathVariable Long hotelId);

}
