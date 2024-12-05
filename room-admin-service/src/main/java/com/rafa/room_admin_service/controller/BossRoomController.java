package com.rafa.room_admin_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafa.room_admin_service.exception.RoomNotFoundException;
import com.rafa.room_admin_service.feign.AuthInterface;
import com.rafa.room_admin_service.feign.HotelInterface;
import com.rafa.room_admin_service.feign.OrderInterface;
import com.rafa.room_admin_service.model.CreateRoomRequest;
import com.rafa.room_admin_service.model.Room;
import com.rafa.room_admin_service.model.dto.OrderDto;
import com.rafa.room_admin_service.model.dto.RoomDto;
import com.rafa.room_admin_service.response.ApiResponse;
import com.rafa.room_admin_service.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/room-admin")
public class BossRoomController {

    @Autowired
    RoomService roomService;

    @Autowired
    HotelInterface hotelInterface;

    @Autowired
    OrderInterface orderInterface;

    @PostMapping("/{hotelId}")
    public ResponseEntity<ApiResponse<List<Room>>> createRooms(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId, @RequestBody List<CreateRoomRequest> requests) {
        System.out.println("準備驗證");
        if (!hotelInterface.checkIsBoss(jwt, hotelId).getBody().getData()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "無權限執行此操作"));
        }
        System.out.println("驗證通過，準備開房間");
        List<Room> newRooms = roomService.createRooms(hotelId, requests);
        if (newRooms != null && !newRooms.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("房間建立成功筆數" + newRooms.size(), newRooms));
        }
        log.error("(createRooms)建立失敗");
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "房間建立失敗，請重新嘗試"));
    }

    ;

    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> deleteRoomsByRoomIds(
            @RequestHeader("Authorization") String jwt,
            @RequestBody List<Long> roomIds) {

        if (roomIds.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "房間ID列表不能為空"));
        }

        log.info("收到刪除房間請求，roomIds: {}", roomIds);

        try {
            Long hotelId = roomService.findHotelIdByRoomId(roomIds.get(0));  // 假設第一個房間的 hotelId 可以代表所有房間
            log.info("找到對應的 hotelId: {}", hotelId);

            ApiResponse<Boolean> authCheck = hotelInterface.checkIsBoss(jwt, hotelId).getBody();
            log.info("權限檢查結果: {}", authCheck);

            if (authCheck == null || !authCheck.getData()) {
                log.warn("權限不足，無法刪除房間");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "無權限執行此操作"));
            }

            boolean deleteResult = roomService.deleteRoomsByRoomIds(roomIds);
            log.info("刪除結果: {}", deleteResult);

            if (deleteResult) {
                return ResponseEntity.ok(
                        ApiResponse.success("成功刪除房間筆數: " + roomIds.size(), null));
            } else {
                log.warn("刪除操作失敗，未刪除任何房間");
                return ResponseEntity.status(500)
                        .body(ApiResponse.error(500, "房間刪除失敗，請稍後再試"));
            }

        } catch (RoomNotFoundException e) {
            log.error("房間刪除過程中發生錯誤", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "房間刪除失敗，請重新嘗試：" + e.getMsg()));
        } catch (Exception e) {
            log.error("刪除房間過程中發生未知錯誤", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "系統錯誤，請稍後再試"+e.getMessage()));
        }
    }


    @GetMapping("/{hotelId}/roomIds")
    public ResponseEntity<ApiResponse<List<Long>>> findRoomIdsByHotelId(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId) {

        try {
            if (!hotelInterface.checkIsBoss(jwt,hotelId).getBody().getData())
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "無權限執行此操作"));
            System.out.println("驗證通過，準備找房間");
            List<Long> roomIds = roomService.getRoomIdsByHotelId(hotelId);

            return ResponseEntity.ok(ApiResponse.success("找到旅店房間id",roomIds));
        } catch (RoomNotFoundException e) {
            log.error("(findRoomIdsByHotelId)" + e.getMsg());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400,"查詢失敗，請重新嘗試"));
    }

    ;

    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<ApiResponse<List<RoomDto>>> findRoomsByHotelId(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId) {
        try {
            if (!hotelInterface.checkIsBoss(jwt,hotelId).getBody().getData())
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "無權限執行此操作"));
            List<RoomDto> rooms = roomService.findRoomByHotelId(hotelId);
            return ResponseEntity.ok(ApiResponse.success("成功查詢旅店房間",rooms));
        } catch (RoomNotFoundException e) {
            log.error("(findRoomsByHotelId)" + e.getMsg());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400,"查詢失敗，請重新嘗試"));
    }

    ;

    @GetMapping("/{hotelId}/roomNames")
    public ResponseEntity<ApiResponse<List<String>>> findRoomNamesByHotelId(@PathVariable Long hotelId) {
        try {
            List<String> rooms = roomService.findRoomNamesByHotelId(hotelId);
            return ResponseEntity.ok(ApiResponse.success("成功查詢旅店房間名稱",rooms));
        } catch (RoomNotFoundException e) {
            log.error("(findRoomNamesByHotelId)" + e.getMsg());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400,"查詢失敗，請重新嘗試"));
    }

    ;

    @GetMapping("allOrders")
    public ResponseEntity<ApiResponse<List<List<List<OrderDto>>>>> getHotelsOrders(@RequestHeader("Authorization") String jwt) {
        try {
            List<Long> bossHotelIds = hotelInterface.findHotelIdsByBoss(jwt).getBody().getData();
            List<List<Long>> hotelsRooms = new ArrayList<>();
            bossHotelIds.forEach(id-> {
                try {
                    hotelsRooms.add(roomService.getRoomIdsByHotelId(id));
                } catch (RoomNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
            List<List<List<OrderDto>>> allOrders = new ArrayList<>();
                    hotelsRooms.parallelStream().forEach(roomIds->
                            allOrders.add(orderInterface.getHotelOrders(jwt,roomIds).getBody().getData()));

            return ResponseEntity.ok(ApiResponse.success("成功獲取所有旅店訂單",allOrders));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400,"獲取失敗，重新嘗試"));
    }

}
