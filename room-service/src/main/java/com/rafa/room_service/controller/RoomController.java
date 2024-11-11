package com.rafa.room_service.controller;

import com.rafa.room_service.exception.RoomException;
import com.rafa.room_service.model.Room;
import com.rafa.room_service.model.roomDto.RoomCardDto;
import com.rafa.room_service.model.roomDto.RoomDto;
import com.rafa.room_service.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/room")
public class RoomController {

    @Autowired
    RoomService roomService;

    @GetMapping("/getRooms/{hotelId}")
    public ResponseEntity<List<RoomCardDto>> getRoomCardsFromHotelId(
            @PathVariable Long hotelId,
            @RequestParam(required = false) Integer people,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {

        try {
            List<RoomCardDto> roomCards =
                    roomService.getRoomCardsByDetailsFromRoomIds(hotelId, people, start, end);
            return new ResponseEntity<>(roomCards, HttpStatus.OK);

        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    ;

    @GetMapping("/getRooms")
    public ResponseEntity<List<RoomCardDto>> getRoomCardsByTimeFromRoomIds(
            @RequestParam List<Long> roomIds,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {
        try {
            List<RoomCardDto> roomCards = roomService.convertRoomsToRoomCards(roomIds, start, end);
            return new ResponseEntity<>(roomCards, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    ;

    @GetMapping("/filterSize")
    public ResponseEntity<List<Long>> filterValidRoomBySize(
            @RequestParam List<Long> roomIds,
            @RequestParam Integer people) {
        try {
            List<Long> validRoomIds = roomService.filterInvalidRoomByDetails(roomIds, people);
            return new ResponseEntity<>(validRoomIds, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    ;


    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<List<RoomDto>> findRoomsByHotelId(@PathVariable Long hotelId) {
        try {
            List<RoomDto> rooms = roomService.findRoomByHotelId(hotelId);
            return new ResponseEntity<>(rooms, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    ;

    @GetMapping("/{hotelId}/roomIds")
    public ResponseEntity<List<Long>> findRoomIdsByHotelId(@PathVariable Long hotelId) {
        try {
            List<Long> roomIds = roomService.findRoomIdsByHotelId(hotelId);
            return new ResponseEntity<>(roomIds, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    ;

    @GetMapping("/roomNames")
    public ResponseEntity<List<String>> findRoomNamesByRoomIds(@RequestParam List<Long> roomId) {
        try {
            List<String> rooms = roomService.findRoomNamesByRoomIds(roomId);
            return new ResponseEntity<>(rooms, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    ;

    @GetMapping("/{roomId}/hotelId")
    public ResponseEntity<Long> findHotelIdByRoomId(@PathVariable Long roomId) {
        try {
            Long hotelId = roomService.findHotelIdByRoomId(roomId);
            return new ResponseEntity<>(hotelId, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    ;

    @GetMapping("/minPrice")
    public ResponseEntity<Integer> getMinPricePerDay(
            @RequestParam List<Long> roomIds,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end,
            @RequestParam(required = false) Integer people) {
        try {
            Integer minPrice = roomService.getHotelMinPricePerDay(roomIds, start, end);
            return new ResponseEntity<>(minPrice, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
