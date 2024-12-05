package com.rafa.comment_service.feign;

import com.rafa.comment_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "room-user",url = "http://room-user.seventao:8080",fallback = RoomServiceFallback.class)//加入要映射的名稱（全大寫）
public interface RoomInterface {
    //加入要映射的方法

    @GetMapping("/room-user/{roomId}/hotelId")
    public ResponseEntity<ApiResponse<Long>> findHotelIdByRoomId(@PathVariable Long roomId);
}

@Component
class RoomServiceFallback implements RoomInterface{

    @Override
    public ResponseEntity<ApiResponse<Long>> findHotelIdByRoomId(Long roomId) {
        System.out.println("(RoomInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }
}
