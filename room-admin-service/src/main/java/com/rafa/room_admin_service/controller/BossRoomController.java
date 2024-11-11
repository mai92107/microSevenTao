package com.rafa.room_admin_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafa.room_admin_service.exception.RoomNotFoundException;
import com.rafa.room_admin_service.feign.AuthInterface;
import com.rafa.room_admin_service.feign.HotelInterface;
import com.rafa.room_admin_service.model.CreateRoomRequest;
import com.rafa.room_admin_service.model.Room;
import com.rafa.room_admin_service.model.dto.RoomDto;
import com.rafa.room_admin_service.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/boss/room")
public class BossRoomController {

    @Autowired
    RoomService roomService;

    @Autowired
    HotelInterface hotelInterface;

    @Autowired
    AuthInterface authInterface;

    @Autowired
    ObjectMapper objectMapper;

    @PostMapping("/{hotelId}")
    public ResponseEntity<String> createRooms(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId, @RequestBody List<CreateRoomRequest> requests) {
        System.out.println("準備驗證");

        if (Boolean.FALSE.equals(hotelInterface.checkIsBoss(jwt, hotelId).getBody()))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        System.out.println("驗證通過，準備開房間");

        String status = roomService.createRooms(hotelId, requests);

        if (status != null) {
            return new ResponseEntity<>(status, HttpStatus.CREATED);
        } else {
            log.error("(createRooms)建立失敗");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    ;

    @DeleteMapping
    public ResponseEntity<String> deleteRoomsByRoomIds(@RequestHeader("Authorization") String jwt, @RequestBody List<Long> roomIds) {
        log.info("使用者jwt為 : " + jwt);
        try {
            Long hotelId = roomService.findHotelIdByRoomId(roomIds.get(0));
            if (Boolean.FALSE.equals(hotelInterface.checkIsBoss(jwt, hotelId).getBody()))
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            System.out.println("驗證通過，準備開房間");
            if (roomService.deleteRoomsByRoomIds(roomIds))
                return new ResponseEntity<>("成功刪除房間編號" + roomIds, HttpStatus.OK);
        } catch (RoomNotFoundException e) {
            log.error("(deleteRoomsByRoomIds)" + e.getMsg());
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    ;

    @GetMapping("/{hotelId}/roomIds")
    public ResponseEntity<List<Long>> findRoomIdsByHotelId(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId) {

        try {
            if (Boolean.FALSE.equals(objectMapper.convertValue(authInterface.validateJwt(jwt).getBody().getData(), Boolean.class)))
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            System.out.println("驗證通過，準備開房間");

            List<Long> roomIds = roomService.getRoomIdsByHotelId(hotelId);

            return new ResponseEntity<>(roomIds, HttpStatus.OK);
        } catch (RoomNotFoundException e) {
            log.error("(findRoomIdsByHotelId)" + e.getMsg());
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    ;

    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<List<RoomDto>> findRoomsByHotelId(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId) {
        try {
            List<RoomDto> rooms = roomService.findRoomByHotelId(hotelId);
            return new ResponseEntity<>(rooms, HttpStatus.OK);
        } catch (RoomNotFoundException e) {
            log.error("(findRoomsByHotelId)" + e.getMsg());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    ;

    @GetMapping("/{hotelId}/roomNames")
    public ResponseEntity<List<String>> findRoomNamesByHotelId(@PathVariable Long hotelId) {
        try {
            List<String> rooms = roomService.findRoomNamesByHotelId(hotelId);
            return new ResponseEntity<>(rooms, HttpStatus.OK);
        } catch (RoomNotFoundException e) {
            log.error("(findRoomNamesByHotelId)" + e.getMsg());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    ;


}
