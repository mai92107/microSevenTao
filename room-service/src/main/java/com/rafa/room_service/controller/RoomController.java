package com.rafa.room_service.controller;

import com.rafa.room_service.model.roomDto.RoomCardDto;
import com.rafa.room_service.model.roomDto.RoomDto;
import com.rafa.room_service.response.ApiResponse;
import com.rafa.room_service.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/room-user")
public class RoomController {

    @Autowired
    RoomService roomService;

    @GetMapping("/getRooms/{hotelId}")
    public ResponseEntity<ApiResponse<List<RoomCardDto>>> getRoomCardsFromHotelId(
            @PathVariable Long hotelId,
            @RequestParam(required = false)Integer people,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        log.info("(getRoomCardsFromHotelId)搜尋開始時間:{} 結束時間:{}",start,end);
        LocalDate checkIn=null;
        LocalDate checkOut=null;
        if(!start.isEmpty() &&!end.isEmpty()) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(("yyyy-MM-dd"));
            checkIn = LocalDate.parse(start, dtf);
            checkOut = LocalDate.parse(end, dtf);
        }
        log.info("(getRoomCardsFromHotelId)轉換後時間:{} 結束時間:{}",checkIn,checkOut);

        try {
            List<RoomCardDto> roomCards =
                    roomService.getRoomCardsByDetailsFromRoomIds(hotelId, people, checkIn, checkOut);
            return ResponseEntity.ok(ApiResponse.success("房間卡取得成功", roomCards));

        } catch (Exception e) {
            log.error("(getRoomCardsFromHotelId)" + e.getMessage());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "房間取得失敗"));
    }

    ;

    @GetMapping("/getRooms")
    public ResponseEntity<ApiResponse<List<RoomCardDto>>> getRoomCardsByTimeFromRoomIds(
            @RequestParam List<Long> roomIds,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        log.info("(getRoomCardsByTimeFromRoomIds)搜尋開始時間:{} 結束時間:{}",start,end);
        LocalDate checkIn=null;
        LocalDate checkOut=null;
        if(start !=null && end != null) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(("yyyy-MM-dd"));
            checkIn = LocalDate.parse(start, dtf);
            checkOut = LocalDate.parse(end, dtf);
        }
        log.info("(getRoomCardsByTimeFromRoomIds)轉換後時間:{} 結束時間:{}",checkIn,checkOut);
        try {
            List<RoomCardDto> roomCards = roomService.convertRoomsToRoomCards(roomIds, checkIn, checkOut);
            return ResponseEntity.ok(ApiResponse.success("成功取得房間卡", roomCards));
        } catch (Exception e) {
            log.error("(getRoomCardsByTimeFromRoomIds)" + e.getMessage());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "房間卡取得失敗"));

    }

    ;

    @GetMapping("/filterSize")
    public ResponseEntity<ApiResponse<List<Long>>> filterValidRoomBySize(
            @RequestParam List<Long> roomIds,
            @RequestParam Integer people) {
        try {
            List<Long> validRoomIds = roomService.filterInvalidRoomByDetails(roomIds, people);
            return ResponseEntity.ok(ApiResponse.success("房間成功過濾", validRoomIds));
        } catch (Exception e) {
            log.error("(filterValidRoomBySize)" + e.getMessage());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "過濾失敗"));
    }

    ;


    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<ApiResponse<List<RoomDto>>> findRoomsByHotelId(@PathVariable Long hotelId) {
        try {
            List<RoomDto> rooms = roomService.findRoomByHotelId(hotelId);
            return ResponseEntity.ok(ApiResponse.success("房間查詢成功", rooms));
        } catch (Exception e) {
            log.error("(findRoomsByHotelId)" + e.getMessage());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "房間查詢失敗"));
    }

    ;

    @GetMapping("/{hotelId}/roomIds")
    public ResponseEntity<ApiResponse<List<Long>>> findRoomIdsByHotelId(@PathVariable Long hotelId) {
        try {
            List<Long> roomIds = roomService.findRoomIdsByHotelId(hotelId);
            return ResponseEntity.ok(ApiResponse.success("房間id查詢成功", roomIds));
        } catch (Exception e) {
            log.error("(findRoomIdsByHotelId)" + e.getMessage());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "房間id查詢失敗"));

    }

    ;

    @GetMapping("/roomNames")
    public ResponseEntity<ApiResponse<List<String>>> findRoomNamesByRoomIds(@RequestParam List<Long> roomId) {
        try {
            List<String> rooms = roomService.findRoomNamesByRoomIds(roomId);
            return ResponseEntity.ok(ApiResponse.success("房間名稱查詢成功", rooms));
        } catch (Exception e) {
            log.error("(findRoomNamesByRoomIds)" + e.getMessage());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "房間名稱查詢失敗"));
    }

    ;

    @GetMapping("/{roomId}/hotelId")
    public ResponseEntity<ApiResponse<Long>> findHotelIdByRoomId(@PathVariable Long roomId) {
        try {
            Long hotelId = roomService.findHotelIdByRoomId(roomId);
            return ResponseEntity.ok(ApiResponse.success("旅店id查詢成功", hotelId));
        } catch (Exception e) {

            log.error("(findHotelIdByRoomId)" + e.getMessage());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "旅店id查詢失敗"));

    }

    ;

    @GetMapping("/minPrice")
    public ResponseEntity<ApiResponse<Integer>> getMinPricePerDay(
            @RequestParam List<Long> roomIds,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end ) {
        try {
            Integer minPrice = roomService.getHotelMinPricePerDay(roomIds, start, end);
            return ResponseEntity.ok(ApiResponse.success("價格查詢成功", minPrice));
        } catch (Exception e) {
            log.error("(getMinPricePerDay)" + e.getMessage());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "價格查詢失敗"));
    }
}
