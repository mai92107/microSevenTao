package com.rafa.order_admin_service.feign;


import com.rafa.order_admin_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient("HOTEL-ADMIN-SERVICE")//加入要映射的名稱（全大寫）
public interface HotelInterface {
    //加入要映射的方法
    @GetMapping("/boss/hotelIds")
    public ResponseEntity<ApiResponse<List<Long>>> findHotelIdsByBoss(@RequestHeader("Authorization") String jwt);

}
