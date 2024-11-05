package com.rafa.order_service.feign;


import com.rafa.order_service.model.dto.RoomDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@FeignClient("ROOM-SERVICE-SERVICE")//加入要映射的名稱（全大寫）
public interface RoomInterface {
    //加入要映射的方法
    @GetMapping("/room/getRooms")
    public ResponseEntity<List<RoomDto>> getRoomCardsByTimeFromRoomIds(@RequestParam List<Long> roomIds, @RequestParam(required = false) LocalDate start, @RequestParam(required = false) LocalDate end);

}
