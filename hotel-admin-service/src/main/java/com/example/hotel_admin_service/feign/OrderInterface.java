package com.example.hotel_admin_service.feign;



import com.example.hotel_admin_service.model.dto.OrderDto;
import com.example.hotel_admin_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "order-admin",url = "http://order-admin.seventao:8080",fallback = OrderServiceFallback.class)//加入要映射的名稱（全大寫）
public interface OrderInterface {
    //加入要映射的方法

    @GetMapping("/order-admin/orders")
    public ResponseEntity<ApiResponse<List<List<OrderDto>>>> getHotelOrders(@RequestHeader("Authorization") String jwt, @RequestParam List<Long> roomIds);
}

@Component
class OrderServiceFallback implements OrderInterface {

    @Override
    public ResponseEntity<ApiResponse<List<List<OrderDto>>>> getHotelOrders(String jwt, List<Long> roomIds){
        System.out.println("(OrderInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }
}
