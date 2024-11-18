package com.rafa.order_admin_service.feign;

import com.rafa.order_admin_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient("ROOM-ADMIN-SERVICE")//加入要映射的名稱（全大寫）
public interface RoomInterface {
    //加入要映射的方法
    @GetMapping("/boss/room/{hotelId}/roomIds")
    public ResponseEntity<ApiResponse<List<Long>>> findRoomIdsByHotelId(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId);

}
