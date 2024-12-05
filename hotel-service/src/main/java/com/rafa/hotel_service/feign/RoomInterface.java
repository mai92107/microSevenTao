package com.rafa.hotel_service.feign;


import com.rafa.hotel_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "room-user",
        url = "http://room-user.seventao:8080",
        fallback = RoomUserFallback.class)
public interface RoomInterface {
    //加入要映射的方法
    @GetMapping("/room-user/filterSize")
    public ResponseEntity<ApiResponse<List<Long>>> filterValidRoomBySize(@RequestParam List<Long> roomIds,
                                                                         @RequestParam Integer people);

    @GetMapping("/room-user/{hotelId}/roomIds")
    public ResponseEntity<ApiResponse<List<Long>>> findRoomIdsByHotelId(@PathVariable Long hotelId);

    @GetMapping("/room-user/roomNames")
    public ResponseEntity<ApiResponse<List<String>>> findRoomNamesByRoomIds(@RequestParam List<Long> roomId);

    @GetMapping("/room-user/minPrice")
    public ResponseEntity<ApiResponse<Integer>> getMinPricePerDay(@RequestParam List<Long> roomIds,
                                                                  @RequestParam String start,
                                                                  @RequestParam String end);
}

@Component
class RoomUserFallback implements RoomInterface{

    @Override
    public ResponseEntity<ApiResponse<List<Long>>> filterValidRoomBySize(List<Long> roomIds, Integer people) {
        System.out.println("(RoomInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }

    @Override
    public ResponseEntity<ApiResponse<List<Long>>> findRoomIdsByHotelId(Long hotelId) {
        System.out.println("(RoomInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));    }

    @Override
    public ResponseEntity<ApiResponse<List<String>>> findRoomNamesByRoomIds(List<Long> roomId) {
        System.out.println("(RoomInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));    }

    @Override
    public ResponseEntity<ApiResponse<Integer>> getMinPricePerDay(List<Long> roomIds, String start, String end) {
        System.out.println("(RoomInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));    }
}
