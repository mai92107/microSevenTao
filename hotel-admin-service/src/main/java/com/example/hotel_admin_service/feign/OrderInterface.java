package com.example.hotel_admin_service.feign;



import com.example.hotel_admin_service.model.Comment;
import com.example.hotel_admin_service.model.dto.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("ORDER-ADMIN-SERVICE")//加入要映射的名稱（全大寫）
public interface OrderInterface {
    //加入要映射的方法

    @GetMapping("/boss/orders")
    public ResponseEntity<List<List<OrderDto>>> getHotelOrders(@RequestHeader("Authorization") String jwt, @RequestParam List<Long> roomIds);
    }
