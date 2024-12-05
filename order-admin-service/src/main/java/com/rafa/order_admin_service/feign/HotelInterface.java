package com.rafa.order_admin_service.feign;


import com.rafa.order_admin_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "hotel-admin",url = "http://hotel-admin.seventao:8080",fallback = HotelServiceFallback.class)//加入要映射的名稱（全大寫）
public interface HotelInterface {
    //加入要映射的方法
    @GetMapping("/hotel-admin/hotelIds")
    public ResponseEntity<ApiResponse<List<Long>>> findHotelIdsByBoss(@RequestHeader("Authorization") String jwt);
}

@Component
class HotelServiceFallback implements HotelInterface {
    @Override
    public ResponseEntity<ApiResponse<List<Long>>> findHotelIdsByBoss(String jwt) {
        System.out.println("(HotelInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }
}