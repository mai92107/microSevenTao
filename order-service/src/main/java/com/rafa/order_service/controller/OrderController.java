package com.rafa.order_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafa.order_service.feign.AuthInterface;
import com.rafa.order_service.feign.RoomInterface;
import com.rafa.order_service.feign.UserInterface;
import com.rafa.order_service.model.Orders;
import com.rafa.order_service.model.dto.*;
import com.rafa.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;

import java.util.List;

@RestController
@RequestMapping("/member/order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    AuthInterface authInterface;

    @Autowired
    UserInterface userInterface;

    @Autowired
    RoomInterface roomInterface;
    @Autowired
    ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<Orders> createOrder(@RequestHeader("Authorization") String jwt, @RequestBody CreateOrderRequest request) {
        try {
            UserDto user = objectMapper.convertValue(userInterface.getUserProfile(jwt).getBody().getData(), UserDto.class);
            if (
                    !request.getCheckInDate().isBefore(LocalDate.now()) &&//不可預訂過去日期
                            request.getCheckInDate().isBefore(request.getCheckOutDate()) &&//審查日期合理性
                            orderService.isRoomAvailable(request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate())) { //不可重複預定
                List<RoomDto> onlyRoom = roomInterface.getRoomCardsByTimeFromRoomIds(List.of(request.getRoomId()), null, null).getBody();
                RoomDto room = new RoomDto();
                if (onlyRoom != null)
                    room = onlyRoom.get(0);
                System.out.println("我要訂這個房間" + room.getRoomName());
                Orders newOrder = new Orders();
                if (user != null)
                    newOrder = orderService.createOrder(user, room.getRoomId(), request.getTotalPrice(), room.getRoomName(), !room.getRoomPic().isEmpty() ? room.getRoomPic().get(0) : "", request.getCheckInDate(), request.getCheckOutDate());
                return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
            } else throw new RuntimeException("訂房失敗，請重新操作");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteInvalidOrderFromUser(@RequestHeader("Authorization") String jwt, @PathVariable long orderId) {
        try {
            Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody().getData(), Long.class);
            System.out.println("我要刪除order" + orderId);
            if (orderService.deleteInvalidOrderFromUser(userId, orderId))

                return new ResponseEntity<>("成功刪除", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("錯誤", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/availability/{roomId}")
    public ResponseEntity<Boolean> checkRoomAvailable(@PathVariable Long roomId, @RequestParam LocalDate start, @RequestParam LocalDate end) {
        try {
            if (orderService.isRoomAvailable(roomId, start, end))
                return new ResponseEntity<>(true, HttpStatus.OK);
            else
                return new ResponseEntity<>(false, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/all")
    public ResponseEntity<List<List<OrderDto>>> getUserAllOrders(@RequestHeader("Authorization") String jwt) {

        Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody().getData(), Long.class);

        List<List<OrderDto>> allOrders = new ArrayList<>();
        try {
            List<OrderDto> validOrders = orderService.getUserValidOrder(userId);
            List<OrderDto> pendingOrders = orderService.getUserPendingOrder(userId);
            List<OrderDto> disannulOrders = orderService.getUserDisannulOrder(userId);
            List<OrderDto> finishedOrders = orderService.getUserFinishedOrder(userId);
            List<OrderDto> canceledOrders = orderService.getUserCanceledOrder(userId);
            System.out.println("使用者" + userId + "有" + validOrders.size() + "筆有效訂單,有" + pendingOrders.size() + "筆等待中訂單,有" + disannulOrders.size() + "筆欲取消訂單,有" + canceledOrders.size() + "筆取消訂單,有" + finishedOrders.size() + "筆完成訂單");
            allOrders.add(validOrders);
            allOrders.add(pendingOrders);
            allOrders.add(disannulOrders);
            allOrders.add(finishedOrders);
            allOrders.add(canceledOrders);
            return new ResponseEntity<>(allOrders, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/availability")
    public ResponseEntity<List<Long>> checkHotelAvailableRooms(@RequestBody CheckRoomAvailableRequest request) {
        try {
            List<Long> hotelRoomAvailable = orderService.filterHotelUnavailableRoom(request.getRoomIds(), request.getStart(), request.getEnd());
            return new ResponseEntity<>(hotelRoomAvailable, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/hotel")
    public ResponseEntity<Integer> getHotelOrderCount(@RequestBody List<Long> roomIds) {
        try {
            System.out.println("我要搜尋訂單數量");
            Integer orders = orderService.getOrderCountByRoomList(roomIds);
            if (orders != null) {
                return new ResponseEntity<>(orders, HttpStatus.OK);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("搜尋訂單失敗" + roomIds);
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<Orders> cancelOrder(@RequestHeader("Authorization") String jwt, @PathVariable Long orderId) {
        try {
            System.out.println("準備刪除訂單");
            Orders order = orderService.cancelOrder(orderId);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/wantCancel/{orderId}")
    public ResponseEntity<Orders> wantCancelOrder(@RequestHeader("Authorization") String jwt, @PathVariable Long orderId) {
        try {
            System.out.println("準備刪除訂單");
            Orders order = orderService.wantCancelOrder(orderId);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Orders> getOrderData(@PathVariable Long orderId) {
        try {
            System.out.println("我要找這筆訂單" + orderId);
            Orders order = orderService.findOrderByOrderId(orderId);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/{orderId}/commented/{status}")
    public ResponseEntity<String> updateOrderCommentStatus(@RequestHeader("Authorization") String jwt,@PathVariable Long orderId,@PathVariable Boolean status) {
        try {
            Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody().getData(), Long.class);
            System.out.println("我要改這筆訂單" + orderId);
            if (orderService.updateOrderCommentStatus(userId, orderId, status))
                return new ResponseEntity<>("修改成功", HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("無法修改，非該評論使用者", HttpStatus.BAD_REQUEST);
    }
}
