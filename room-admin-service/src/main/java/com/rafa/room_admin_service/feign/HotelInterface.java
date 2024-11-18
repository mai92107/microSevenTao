package com.rafa.room_admin_service.feign;


import com.rafa.room_admin_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient("HOTEL-ADMIN-SERVICE")//加入要映射的名稱（全大寫）
public interface HotelInterface {
    //加入要映射的方法

    @GetMapping("/boss/findBoss/{hotelId}")
    public ResponseEntity<ApiResponse<Boolean>> checkIsBoss(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId);

    @GetMapping("/boss/hotelIds")
    public ResponseEntity<ApiResponse<List<Long>>> findHotelIdsByBoss(@RequestHeader("Authorization") String jwt);
}
