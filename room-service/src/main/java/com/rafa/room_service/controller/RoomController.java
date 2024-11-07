package com.rafa.room_service.controller;

import com.rafa.room_service.model.Room;
import com.rafa.room_service.model.roomDto.RoomCardDto;
import com.rafa.room_service.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/room")
public class RoomController {

    @Autowired
    RoomService roomService;

    @GetMapping("/getRooms/{hotelId}")
    public ResponseEntity<List<RoomCardDto>> getRoomCardsFromHotelId(@PathVariable Long hotelId, @RequestParam(required = false) Integer people, @RequestParam(required = false) LocalDate start, @RequestParam(required = false) LocalDate end) {
        try {
            List<Long> roomIds = roomService.findRoomIdsByHotelId(hotelId);
            System.out.println("我要轉換room數量" + roomIds.size());
            if (people != null)
                roomIds = roomService.filterInvalidRoomByDetails(roomIds, people);
            List<RoomCardDto> roomCards = roomIds.stream().map(rid -> roomService.convertRoomToRoomCard(rid, start, end)).toList();
            System.out.println("轉換完畢");
            return new ResponseEntity<>(roomCards, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus .BAD_REQUEST);
        }
    };

    @GetMapping("/getRooms")
    public ResponseEntity<List<RoomCardDto>> getRoomCardsByTimeFromRoomIds(@RequestParam List<Long> roomIds, @RequestParam(required = false) LocalDate start, @RequestParam(required = false) LocalDate end){
        try {
            List<RoomCardDto> roomCards = roomIds.stream().map(rid -> roomService.convertRoomToRoomCard(rid, start, end)).toList();
            System.out.println("轉換完畢");
            return new ResponseEntity<>(roomCards, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus .BAD_REQUEST);
        }
    };

    @GetMapping("/filterSize")
    public ResponseEntity<List<Long>> filterValidRoomBySize(@RequestParam List<Long> roomIds, @RequestParam Integer people) {
        try {
            System.out.println("篩選房間"+people);
            List<Long> validRoomIds = roomService.filterInvalidRoomByDetails(roomIds, people);
            return new ResponseEntity<>(validRoomIds, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    };


    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<List<Room>> findRoomsByHotelId(@PathVariable Long hotelId) {
        try {
            List<Room> rooms = roomService.findRoomByHotelId(hotelId);
            System.out.println("搜尋這個旅店" + hotelId);
            return new ResponseEntity<>(rooms, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    ;

    @GetMapping("/{hotelId}/roomIds")
    public ResponseEntity<List<Long>> findRoomIdsByHotelId(@PathVariable Long hotelId) {
        try {
            System.out.println("搜尋房間id");
            List<Long> roomIds = roomService.findRoomIdsByHotelId(hotelId);
            return new ResponseEntity<>(roomIds, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    ;

    @GetMapping("/roomNames")
    public ResponseEntity<List<String>> findRoomNamesByRoomIds(@RequestParam List<Long> roomId) {
        try {
            List<String> rooms = roomService.findRoomNamesByRoomIds(roomId);
            return new ResponseEntity<>(rooms, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    ;

    @GetMapping("/{roomId}/hotelId")
    public ResponseEntity<Long> findHotelIdByRoomId(@PathVariable Long roomId) {
        try {
            Long hotelId = roomService.findHotelIdByRoomId(roomId);
            return new ResponseEntity<>(hotelId, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    ;

    @GetMapping("/minPrice")
    public ResponseEntity<Integer> getMinPricePerDay(@RequestParam List<Long> roomIds, @RequestParam LocalDate start, @RequestParam LocalDate end, @RequestParam(required = false) Integer people) {
        try {
            Integer minPrice = roomService.getHotelMinPricePerDay(roomIds, start, end);
            return new ResponseEntity<>(minPrice, HttpStatus.OK);

        } catch (RuntimeException e) {
            e.printStackTrace();
            return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
        }
    }
}
