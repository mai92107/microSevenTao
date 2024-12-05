package com.example.hotel_admin_service.controller;

import com.example.hotel_admin_service.exception.HotelNotFoundException;
import com.example.hotel_admin_service.feign.AuthInterface;
import com.example.hotel_admin_service.feign.RoomInterface;
import com.example.hotel_admin_service.feign.UserInterface;
import com.example.hotel_admin_service.model.dto.*;
import com.example.hotel_admin_service.response.ApiResponse;
import com.example.hotel_admin_service.service.HotelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/hotel-admin")
@Slf4j
public class BossHotelController {

    @Autowired
    HotelService hotelService;

    @Autowired
    AuthInterface authInterface;

    @Autowired
    UserInterface userInterface;


    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RoomInterface roomInterface;

    @PostMapping
    public ResponseEntity<ApiResponse<HotelDto>> createHotel(@RequestHeader("Authorization") String jwt, @RequestBody CreateHotelRequest request) {
        UserDto user = userInterface.getUserProfile(jwt).getBody().getData();
        if (!user.getROLE().equals("ROLE_HOTELER")) {
            log.error("(createHotel)" + user.getUserId() + "試圖新增旅店");
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "無權限執行此操作"));
        }

        HotelDto hotel = hotelService.createHotel(user.getUserId(), request);
        if (hotel != null)
            return ResponseEntity.ok(ApiResponse.success("新增成功", hotel));
        else
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "新增失敗，請重新嘗試"));
    }

    ;

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<ApiResponse<String>> deleteHotelByHotelId(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId) {
        Long userId = authInterface.findUserIdByJwt(jwt).getBody().getData();
        try {
            if (!hotelService.validateBoss(userId, hotelId)) {
                log.error("(deleteHotelByHotelId)" + userId + "試圖修改旅店" + hotelId + "資料");
                return ResponseEntity.badRequest().body(new ApiResponse<>(400, "無權限執行此操作", null));
            }
            hotelService.deleteHotelByHotelId(hotelId);
            return ResponseEntity.ok(ApiResponse.success("刪除成功", null));
        } catch (HotelNotFoundException e) {
            log.error("(deleteHotelByHotelId)" + e.getMsg());
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "刪除失敗，請重新嘗試"));
        }
    }

    ;

    @PutMapping("/{hotelId}")
    public ResponseEntity<ApiResponse<HotelDto>> updateHotelData(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId, @RequestBody CreateHotelRequest request) {
        try {
            Long userId = authInterface.findUserIdByJwt(jwt).getBody().getData();
            if (hotelService.validateBoss(userId, hotelId)) {
                HotelDto hotel = hotelService.updateHotelData(hotelId, request);
                return ResponseEntity.ok(ApiResponse.success("修改成功", hotel));
            }
            log.error("(updateHotelData)" + userId + "試圖修改旅店" + hotelId + "的資料");
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "無權限執行此操作", null));
        } catch (HotelNotFoundException e) {
            log.error("(updateHotelData)" + e.getMsg());
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "修改失敗，請重新嘗試"));
        }
    };

    @GetMapping("/hotels")
    public ResponseEntity<ApiResponse<List<HotelCardDto>>> findHotelsByBoss(@RequestHeader("Authorization") String jwt) {
        log.info("(findHotelsByBoss)使用者jwt:" + jwt);
        Long userId = authInterface.findUserIdByJwt(jwt).getBody().getData();
        System.out.println("我要找老闆的飯店" + userId);
        try {
            List<HotelCardDto> myHotels = hotelService.findHotelsByBoss(userId);
            return ResponseEntity.ok(ApiResponse.success("找到老闆的旅店" + userId, myHotels));
        } catch (HotelNotFoundException e) {
            log.error("(findHotelsByBoss)" + e.getMsg());
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "查詢失敗，請重新嘗試"));
        }

    };


    @GetMapping("/hotelIds")
    public ResponseEntity<ApiResponse<List<Long>>> findHotelIdsByBoss(@RequestHeader("Authorization") String jwt) {
        log.info("(findHotelIdsByBoss)使用者jwt:" + jwt);
        Long userId = authInterface.findUserIdByJwt(jwt).getBody().getData();
        System.out.println("我要找老闆的飯店" + userId);
        List<Long> myHotelIds = hotelService.findHotelIdsByBossId(userId);
        return ResponseEntity.ok(ApiResponse.success("找到老闆的旅店" + userId, myHotelIds));
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<ApiResponse<HotelDto>> findHotelByHotelId(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId) {
        try {
            Long userId =authInterface.findUserIdByJwt(jwt).getBody().getData();
            if (!hotelService.validateBoss(userId, hotelId))
                return ResponseEntity.badRequest().body(new ApiResponse<>(400, "無權限執行此操作", null));
            HotelDto hotel = hotelService.findHotelDtoByHotelId(hotelId);
            return ResponseEntity.ok(ApiResponse.success("找到此旅店" + hotelId, hotel));
        } catch (HotelNotFoundException e) {
            log.error("(findHotelByHotelId)");
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "查詢失敗，請重新嘗試"));
        }
    }

//    @PutMapping("/{hotelId}/score")
//    public ResponseEntity<ApiResponse<HotelDto>> updateHotelScore(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId, @RequestBody Double score) {
//        try {
//            Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody().getData(), Long.class);
//            if (hotelService.validateBoss(userId, hotelId)) {
//                HotelDto hotel = hotelService.updateHotelScore(hotelId, score);
//                return ResponseEntity.ok(ApiResponse.success("修改完成", hotel));
//            }
//            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "無權限執行此操作", null));
//        } catch (HotelNotFoundException e) {
//            log.error("(updateHotelScore)" + e.getMsg());
//        }
//        return ResponseEntity.badRequest().body(ApiResponse.error(400, "修改失敗，請重新嘗試"));
//    }

    @GetMapping("/findBoss/{hotelId}")
    public ResponseEntity<ApiResponse<Boolean>> checkIsBoss(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId) {
        try {
            Long userId = authInterface.findUserIdByJwt(jwt).getBody().getData();
            Boolean result = hotelService.validateBoss(userId, hotelId);
            if (!result)
                log.error("(checkIsBoss)使用者" + userId + "嘗試執行非法操作！！");
            return ResponseEntity.ok(ApiResponse.success("查詢成功", result));

        } catch (HotelNotFoundException e) {
            log.error("(checkIsBoss)" + e.getMsg());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "查詢失敗，請重新嘗試"));
    }


    //    @GetMapping("/allOrders")
//    public ResponseEntity<ApiResponse<Map<String, List<List<OrderDto>>>>> findBossOrders(@RequestHeader("Authorization") String jwt) {
//        try {
//            Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody().getData(), Long.class);
//            System.out.println("查找使用者 ID: " + userId);
//
//            List<Long> bossHotels = hotelService.findHotelIdsByBossId(userId);
//            System.out.println("使用者擁有的旅店 ID: " + bossHotels);
//
//            Map<String, List<List<OrderDto>>> hotelOrders = new HashMap<>();
//            for (Long hotelId : bossHotels) {
//                HotelDto hotel = hotelService.findHotelDtoByHotelId(hotelId);
//                List<Long> roomIds = roomInterface.findRoomIdsByHotelId(jwt, hotelId).getBody().getData();
//                List<List<OrderDto>> hotelOrder = new ArrayList<>();
//
//                if (roomIds != null && !roomIds.isEmpty()) {
//                    hotelOrder = orderInterface.getHotelOrders(jwt, roomIds).getBody().getData();
//                    hotelOrders.put(hotel.getChName(), hotelOrder);
//                }
//            }
//
//            return ResponseEntity.ok(ApiResponse.success("查詢成功", hotelOrders));
//
//        } catch (Exception e) {
//            log.error("獲取訂單時發生錯誤: " + e.getMessage());
//            return ResponseEntity.badRequest().body(ApiResponse.error(400, "查詢失敗，請重新嘗試"));
//        }
//    }
    @GetMapping("/hotelAllRooms")
    public ResponseEntity<ApiResponse<List<GetHotelRoomResquest>>> gethotelRooms(@RequestHeader("Authorization") String jwt) {
        try {
            Long userId = authInterface.findUserIdByJwt(jwt).getBody().getData();
            System.out.println("查找使用者 ID: " + userId);

            List<GetHotelRoomResquest> hotelsRooms = hotelService.findHotelsWithRoomsByBoss(jwt,userId);
            return ResponseEntity.ok(ApiResponse.success("查詢成功", hotelsRooms));
        } catch (Exception e) {
            log.error("獲取旅店時發生錯誤: " + e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "查詢失敗，請重新嘗試" + e.getMessage()));
        }
    }

}
