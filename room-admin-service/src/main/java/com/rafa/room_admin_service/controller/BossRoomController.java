package com.rafa.room_admin_service.controller;

import com.rafa.room_admin_service.feign.AuthInterface;
import com.rafa.room_admin_service.feign.HotelInterface;
import com.rafa.room_admin_service.model.CreateRoomRequest;
import com.rafa.room_admin_service.model.Room;
import com.rafa.room_admin_service.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boss/room")
public class BossRoomController {

    @Autowired
    RoomService roomService;

    @Autowired
    HotelInterface hotelInterface;

    @Autowired
    AuthInterface authInterface;

    @PostMapping("/{hotelId}")
    public ResponseEntity<Room> createRoom(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId, @RequestBody CreateRoomRequest request) {
        System.out.println("準備驗證");

        if (Boolean.FALSE.equals(hotelInterface.checkIsBoss(jwt, hotelId).getBody()))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        System.out.println("驗證通過，準備開房間");
        Room room = roomService.createRoom(hotelId, request);
        if (room != null) {
            System.out.println("成功開了一個room" + room.getRoomName());
            return new ResponseEntity<>(room, HttpStatus.CREATED);
        } else
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    ;

    @DeleteMapping("/{roomId}")
    public ResponseEntity<String> deleteRoomByRoomId(@RequestHeader("Authorization") String jwt, @PathVariable Long roomId) {
        Long hotelId = roomService.findHotelIdByRoomId(roomId);
        if (Boolean.FALSE.equals(hotelInterface.checkIsBoss(jwt, hotelId).getBody()))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (roomService.deleteRoomByRoomId(roomId))
            return new ResponseEntity<>("成功刪除房間編號" + roomId, HttpStatus.OK);
        else
            return new ResponseEntity<>("編號" + roomId + "房間刪除失敗", HttpStatus.BAD_REQUEST);
    }

    ;

    @GetMapping("/{hotelId}/roomIds")
    public ResponseEntity<List<Long>> findRoomIdsByHotelId(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId) {

        try {
            if(Boolean.FALSE.equals(authInterface.validateJwt(jwt).getBody()))
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            List<Long> roomIds = roomService.getRoomIdsByHotelId(hotelId);

            return new ResponseEntity<>(roomIds, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    };

    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<List<Room>> findRoomsByHotelId(@PathVariable Long hotelId) {
        try {
            List<Room> rooms = roomService.findRoomByHotelId(hotelId);
            return new ResponseEntity<>(rooms, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    };

    @GetMapping("/{hotelId}/roomNames")
    public ResponseEntity<List<String>> findRoomNamesByHotelId(@PathVariable Long hotelId){
        try {
            System.out.println("接收到"+hotelId);
            List<String> rooms = roomService.findRoomNamesByHotelId(hotelId);
            System.out.println("接收到"+rooms);
            return new ResponseEntity<>(rooms, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    };


}
