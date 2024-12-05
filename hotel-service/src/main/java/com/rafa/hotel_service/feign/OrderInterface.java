package com.rafa.hotel_service.feign;


import com.rafa.hotel_service.model.dto.CheckRoomAvailableByOrder;
import com.rafa.hotel_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name= "order-user",url = "http://order-user.seventao:8080",fallback = OrderUserFallback.class)//加入要映射的名稱（全大寫）
public interface OrderInterface {
    //加入要映射的方法
    @PostMapping("/order-user/hotel")
    public ResponseEntity<ApiResponse<Integer>> getHotelOrderCount(@RequestBody List<Long> roomIds);

    @PostMapping("/order-user/availability")
    public ResponseEntity<ApiResponse<List<Long>>> checkHotelAvailableRooms(@RequestBody CheckRoomAvailableByOrder request);
}

@Component
class OrderUserFallback implements OrderInterface{

    @Override
    public ResponseEntity<ApiResponse<Integer>> getHotelOrderCount(List<Long> roomIds) {
        System.out.println("(OrderInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }

    @Override
    public ResponseEntity<ApiResponse<List<Long>>> checkHotelAvailableRooms(CheckRoomAvailableByOrder request) {
        System.out.println("(OrderInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }
}
