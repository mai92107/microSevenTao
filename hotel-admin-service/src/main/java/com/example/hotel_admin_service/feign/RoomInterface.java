package com.example.hotel_admin_service.feign;


import com.example.hotel_admin_service.model.dto.RoomDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@FeignClient("ROOM-ADMIN-SERVICE")//加入要映射的名稱（全大寫）
public interface RoomInterface {
    //加入要映射的方法
    @GetMapping("/boss/room/{hotelId}/roomIds")
    public ResponseEntity<List<Long>> findRoomIdsByHotelId(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId);

    @GetMapping("/boss/room/{hotelId}/rooms")
    public ResponseEntity<List<RoomDto>> findRoomsByHotelId(@PathVariable Long hotelId);

    @GetMapping("/boss/room/{hotelId}/roomNames")
    public ResponseEntity<List<String>> findRoomNamesByHotelId(@PathVariable Long hotelId);
}