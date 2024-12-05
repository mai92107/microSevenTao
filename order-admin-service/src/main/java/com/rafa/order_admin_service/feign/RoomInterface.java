package com.rafa.order_admin_service.feign;

import com.rafa.order_admin_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "room-admin",url = "http://room-admin.seventao:8080",fallback = RoomServiceFallback.class)//加入要映射的名稱（全大寫）
public interface RoomInterface {
    //加入要映射的方法
    @GetMapping("/room-admin/{hotelId}/roomIds")
    public ResponseEntity<ApiResponse<List<Long>>> findRoomIdsByHotelId(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId);
}
@Component
class RoomServiceFallback implements RoomInterface{

    @Override
    public ResponseEntity<ApiResponse<List<Long>>> findRoomIdsByHotelId(String jwt, Long hotelId) {
        System.out.println("(HotelInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }
}
