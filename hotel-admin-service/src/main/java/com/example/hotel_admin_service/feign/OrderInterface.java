package com.example.hotel_admin_service.feign;



import com.example.hotel_admin_service.model.dto.OrderDto;
import com.example.hotel_admin_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("ORDER-ADMIN-SERVICE")//加入要映射的名稱（全大寫）
public interface OrderInterface {
    //加入要映射的方法

    @GetMapping("/boss/orders")
    public ResponseEntity<ApiResponse<List<List<OrderDto>>>> getHotelOrders(@RequestHeader("Authorization") String jwt, @RequestParam List<Long> roomIds);
    }
