package com.rafa.comment_service.feign;

import com.rafa.comment_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient("ROOM-USER-SERVICE")//加入要映射的名稱（全大寫）
public interface RoomInterface {
    //加入要映射的方法

    @GetMapping("/room/{roomId}/hotelId")
    public ResponseEntity<ApiResponse<Long>> findHotelIdByRoomId(@PathVariable Long roomId);
}
