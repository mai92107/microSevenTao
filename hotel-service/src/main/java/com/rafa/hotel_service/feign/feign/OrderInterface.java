package com.rafa.hotel_service.feign.feign;


import com.rafa.hotel_service.model.dto.CheckRoomAvailableByOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("ORDER-USER-SERVICE")//加入要映射的名稱（全大寫）
public interface OrderInterface {
    //加入要映射的方法
    @PostMapping("/member/order/hotel")
    public ResponseEntity<Integer> getHotelOrderCount(@RequestBody List<Long> roomIds);

    @PostMapping("/member/order/availability")
    public ResponseEntity<List<Long>> checkHotelAvailableRooms(@RequestBody CheckRoomAvailableByOrder request);
}
