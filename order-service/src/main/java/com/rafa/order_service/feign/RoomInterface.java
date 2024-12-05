package com.rafa.order_service.feign;


import com.rafa.order_service.model.dto.RoomDto;
import com.rafa.order_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "room-user",url = "http://room-user.seventao:8080",fallback = RoomServiceFallback.class)//加入要映射的名稱（全大寫）
public interface RoomInterface {
    //加入要映射的方法
    @GetMapping("/room-user/getRooms")
    public ResponseEntity<ApiResponse<List<RoomDto>>> getRoomCardsByTimeFromRoomIds(@RequestParam List<Long> roomIds,
                                                                                    @RequestParam(required = false) String start,
                                                                                    @RequestParam(required = false) String end);

}

@Component
class RoomServiceFallback implements RoomInterface{
    @Override
    public ResponseEntity<ApiResponse<List<RoomDto>>> getRoomCardsByTimeFromRoomIds(List<Long> roomIds, String start, String end) {
        System.out.println("(HotelInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }
}
