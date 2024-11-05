package com.rafa.comment_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient("Hotel-ADMIN-SERVICE")//加入要映射的名稱（全大寫）
public interface HotelInterface {
    //加入要映射的方法

    @PutMapping("/boss/{hotelId}/score")
    public ResponseEntity<String> updateHotelScore(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId, @RequestBody Double score);

}
