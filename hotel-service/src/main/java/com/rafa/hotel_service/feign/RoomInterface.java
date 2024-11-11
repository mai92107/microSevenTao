package com.rafa.hotel_service.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@FeignClient("ROOM-USER-SERVICE")//加入要映射的名稱（全大寫）
public interface RoomInterface {
    //加入要映射的方法
    @GetMapping("/room/filterSize")
    public ResponseEntity<List<Long>> filterValidRoomBySize(@RequestParam List<Long> roomIds, @RequestParam Integer people);

    @GetMapping("/room/{hotelId}/roomIds")
    public ResponseEntity<List<Long>> findRoomIdsByHotelId(@PathVariable Long hotelId);

    @GetMapping("/room/roomNames")
    public ResponseEntity<List<String>> findRoomNamesByRoomIds(@RequestParam List<Long> roomId);

    @GetMapping("/room/minPrice")
    public ResponseEntity<Integer> getMinPricePerDay(@RequestParam List<Long> roomIds,@RequestParam LocalDate start,@RequestParam LocalDate end);
}
