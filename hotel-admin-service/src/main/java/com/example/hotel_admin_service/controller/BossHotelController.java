package com.example.hotel_admin_service.controller;

import com.example.hotel_admin_service.feign.AuthInterface;
import com.example.hotel_admin_service.feign.OrderInterface;
import com.example.hotel_admin_service.feign.RoomInterface;
import com.example.hotel_admin_service.model.Hotel;
import com.example.hotel_admin_service.model.dto.CreateHotelRequest;
import com.example.hotel_admin_service.model.dto.HotelCardDto;
import com.example.hotel_admin_service.model.dto.HotelDto;
import com.example.hotel_admin_service.model.dto.OrderDto;
import com.example.hotel_admin_service.service.HotelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/boss")
public class BossHotelController {

    @Autowired
    HotelService hotelService;

    @Autowired
    AuthInterface authInterface;

    @Autowired
    OrderInterface orderInterface;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RoomInterface roomInterface;

    @PostMapping("/hotel")
    public ResponseEntity<String> createHotel(@RequestHeader("Authorization") String jwt, @RequestBody CreateHotelRequest request) {
        Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody(), Long.class);
        Hotel hotel = hotelService.createHotel(userId, request);
        if (hotel != null)
            return new ResponseEntity<>("新增成功", HttpStatus.CREATED);
        else
            return new ResponseEntity<>("新增失敗，請重新嘗試", HttpStatus.BAD_REQUEST);
    }

    ;

    @DeleteMapping("/hotel/{hotelId}")
    public ResponseEntity<String> deleteHotelByHotelId(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId) {
        Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody(), Long.class);
        Hotel hotel = hotelService.findHotelByHotelId(hotelId);
        if (!Objects.equals(userId, hotel.getBossId()))
            return new ResponseEntity<>("無權限執行此操作", HttpStatus.BAD_REQUEST);

        boolean success = hotelService.deleteHotelByHotelId(hotelId);
        if (success)
            return new ResponseEntity<>("刪除成功", HttpStatus.OK);
        else
            return new ResponseEntity<>("刪除失敗，請重新操作", HttpStatus.BAD_REQUEST);
    }

    ;

    @PutMapping("/hotel/{hotelId}")
    public ResponseEntity<String> updateHotelData(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId, @RequestBody CreateHotelRequest request) {
        if (!objectMapper.convertValue(authInterface.validateJwt(jwt).getBody().getData(), Boolean.class))
            return new ResponseEntity<>("無權限執行此操作", HttpStatus.BAD_REQUEST);
        Hotel hotel = hotelService.updateHotelData(hotelId, request);
        if (hotel != null)
            return new ResponseEntity<>("更新成功", HttpStatus.OK);
        else
            return new ResponseEntity<>("更新失敗，請重新操作", HttpStatus.BAD_REQUEST);
    }

    ;

    @GetMapping("/hotels")
    public ResponseEntity<List<HotelCardDto>> findHotelsByBoss(@RequestHeader("Authorization") String jwt) {
        Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody(), Long.class);
        System.out.println("我要找老闆的飯店" + userId);
        List<HotelCardDto> myHotels = hotelService.findHotelsByBoss(userId);
        if (myHotels != null)
            return new ResponseEntity<>(myHotels, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    ;

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDto> findHotelByHotelId(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId) {
        try {
            if (Boolean.FALSE.equals(authInterface.validateJwt(jwt).getBody()))
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            HotelDto hotel = hotelService.findHotelDtoByHotelId(hotelId);
            System.out.println("搜尋這個hotel: " + hotel.getChName());
            return new ResponseEntity<>(hotel, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

    }

    @PutMapping("/{hotelId}/score")
    public ResponseEntity<String> updateHotelScore(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId, @RequestBody Double score) {
        try {
            if (Boolean.TRUE.equals(objectMapper.convertValue(authInterface.validateJwt(jwt).getBody().getData(), Boolean.class))){
                hotelService.updateHotelScore(hotelId, score);
                return new ResponseEntity<>("修改完成", HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("分數修改失敗", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/findBoss/{hotelId}")
    public ResponseEntity<Boolean> checkIsBoss(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId) {
        try {
            Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody(), Long.class);
            Boolean result = hotelService.validateBoss(userId, hotelId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/allOrders")
    public ResponseEntity<Map<String, List<List<OrderDto>>>> findBossOrders(@RequestHeader("Authorization") String jwt) {
        try {
            Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody(), Long.class);
            System.out.println("查找使用者 ID: " + userId);

            List<Long> bossHotels = hotelService.findHotelIdsByBossId(userId);
            System.out.println("使用者擁有的旅店 ID: " + bossHotels);

            Map<String, List<List<OrderDto>>> hotelOrders = new HashMap<>();
            for (Long hotelId : bossHotels) {
                Hotel hotel = hotelService.findHotelByHotelId(hotelId);
                List<Long> roomIds = roomInterface.findRoomIdsByHotelId(jwt, hotelId).getBody();
                List<List<OrderDto>> hotelOrder = new ArrayList<>();

                if (roomIds != null && !roomIds.isEmpty()) {
                    hotelOrder = orderInterface.getHotelOrders(jwt, roomIds).getBody();
                }

                hotelOrders.put(hotel.getChName(), hotelOrder);

            }

            return new ResponseEntity<>(hotelOrders, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("獲取訂單時發生錯誤: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

}
