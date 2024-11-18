package com.rafa.order_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafa.order_service.feign.AuthInterface;
import com.rafa.order_service.feign.RoomInterface;
import com.rafa.order_service.feign.UserInterface;
import com.rafa.order_service.model.Orders;
import com.rafa.order_service.model.dto.*;
import com.rafa.order_service.response.ApiResponse;
import com.rafa.order_service.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;

import java.util.List;

@RestController
@RequestMapping("/member/order")
@Slf4j
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
    public ResponseEntity<ApiResponse<Orders>> createOrder(@RequestHeader("Authorization") String jwt, @RequestBody CreateOrderRequest request) {
        try {
            UserDto user = objectMapper.convertValue(userInterface.getUserProfile(jwt).getBody().getData(), UserDto.class);
            if (
                    !request.getCheckInDate().isBefore(LocalDate.now()) &&//不可預訂過去日期
                            request.getCheckInDate().isBefore(request.getCheckOutDate()) &&//審查日期合理性
                            orderService.isRoomAvailable(request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate())) { //不可重複預定

                List<RoomDto> onlyRoom = roomInterface.getRoomCardsByTimeFromRoomIds(List.of(request.getRoomId()), null, null).getBody().getData();
                RoomDto room = new RoomDto();
                if (onlyRoom != null)
                    room = onlyRoom.get(0);
                System.out.println("我要訂這個房間" + room.getRoomName());
                Orders newOrder = new Orders();
                if (user != null)
                    newOrder = orderService.createOrder(user, room.getRoomId(), request.getTotalPrice(), room.getRoomName(), !room.getRoomPic().isEmpty() ? room.getRoomPic().get(0) : "", request.getCheckInDate(), request.getCheckOutDate());
                return ResponseEntity.ok(ApiResponse.success("預訂成功", newOrder));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "訂房資料錯誤，請重新確認"));
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "預訂失敗，請重新操作"));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<String>> deleteInvalidOrderFromUser(@RequestHeader("Authorization") String jwt, @PathVariable long orderId) {
        try {
            Long userId = authInterface.findUserIdByJwt(jwt).getBody().getData();
            Orders order = orderService.findOrderByOrderId(orderId);
            if (userId != order.getUserId())
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "無權限執行此操作"));
            System.out.println("我要刪除order" + orderId);
            if (orderService.deleteInvalidOrderFromUser(userId, orderId))
                return ResponseEntity.ok(ApiResponse.success("成功刪除", null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "訂單刪除失敗，請重新嘗試"));
    }

    @GetMapping("/availability/{roomId}")
    public ResponseEntity<ApiResponse<Boolean>> checkRoomAvailable(@PathVariable Long roomId, @RequestParam LocalDate start, @RequestParam LocalDate end) {
        try {
            Boolean check = orderService.isRoomAvailable(roomId, start, end);
            return ResponseEntity.ok(ApiResponse.success("查詢成功", check));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "查詢失敗"));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<List<OrderDto>>>> getUserAllOrders(@RequestHeader("Authorization") String jwt) {

        Long userId = authInterface.findUserIdByJwt(jwt).getBody().getData();

        List<List<OrderDto>> allOrders = new ArrayList<>();
        try {
            List<OrderDto> validOrders = orderService.getUserValidOrder(userId);
            List<OrderDto> pendingOrders = orderService.getUserPendingOrder(userId);
            List<OrderDto> disannulOrders = orderService.getUserDisannulOrder(userId);
            List<OrderDto> finishedOrders = orderService.getUserFinishedOrder(userId);
            List<OrderDto> canceledOrders = orderService.getUserCanceledOrder(userId);
            allOrders.add(validOrders);
            allOrders.add(pendingOrders);
            allOrders.add(disannulOrders);
            allOrders.add(finishedOrders);
            allOrders.add(canceledOrders);
            return ResponseEntity.ok(ApiResponse.success("成功查詢使用者訂單", allOrders));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "查詢失敗，請重新嘗試"));
    }

    @PostMapping("/availability")
    public ResponseEntity<ApiResponse<List<Long>>> checkHotelAvailableRooms(@RequestBody CheckRoomAvailableRequest request) {
        try {
            List<Long> hotelRoomAvailable = orderService.filterHotelUnavailableRoom(request.getRoomIds(), request.getStart(), request.getEnd());
            return ResponseEntity.ok(ApiResponse.success("查詢成功", hotelRoomAvailable));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "查詢失敗，請重新嘗試"));
    }

    @PostMapping("/hotel")
    public ResponseEntity<ApiResponse<Integer>> getHotelOrderCount(@RequestBody List<Long> roomIds) {
        try {
            System.out.println("我要搜尋訂單數量");
            Integer orders = orderService.getOrderCountByRoomList(roomIds);
            if (orders != null) {
                return ResponseEntity.ok(ApiResponse.success("查詢成功", orders));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "查詢失敗，請重新嘗試"));
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<ApiResponse<String>> cancelOrder(@RequestHeader("Authorization") String jwt, @PathVariable Long orderId) {
        try {
            Long userId = authInterface.findUserIdByJwt(jwt).getBody().getData();
            Orders order = orderService.findOrderByOrderId(orderId);
            if (order.getUserId() != userId) {
                log.error("(cancelOrder)使用者"+userId+"嘗試執行非法操作");
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "無權限執行此操作"));
            }

            orderService.cancelOrder(orderId);
            return ResponseEntity.ok(ApiResponse.success("訂單取消成功",null));
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400,"操作失敗，請重新嘗試"));
    }

    @PutMapping("/wantCancel/{orderId}")
    public ResponseEntity<ApiResponse<Orders>> wantCancelOrder(@RequestHeader("Authorization") String jwt, @PathVariable Long orderId) {
        try {
            Long userId = authInterface.findUserIdByJwt(jwt).getBody().getData();
            Orders order = orderService.findOrderByOrderId(orderId);
            if (order.getUserId() != userId) {
                log.error("(wantCancelOrder)使用者"+userId+"嘗試執行非法操作");
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "無權限執行此操作"));
            }
            orderService.wantCancelOrder(orderId);
            return ResponseEntity.ok(ApiResponse.success("取消訂單需求成功提出",null));
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400,"操作失敗，請重新嘗試"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Orders>> getOrderData(@PathVariable Long orderId) {
        try {
            Orders order = orderService.findOrderByOrderId(orderId);
            System.out.println("我要找這筆訂單" + orderId);
            return ResponseEntity.ok(ApiResponse.success("查詢成功",order));
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400,"操作失敗，請重新嘗試"));
    }

    @PutMapping("/{orderId}/commented/{status}")
    public ResponseEntity<ApiResponse<String>> updateOrderCommentStatus(@RequestHeader("Authorization") String jwt,
                                                                        @PathVariable Long orderId,
                                                                        @PathVariable Boolean status){
        try{
            Long userId = authInterface.findUserIdByJwt(jwt).getBody().getData();
            Orders order = orderService.findOrderByOrderId(orderId);
            if(userId!=order.getUserId()) {
                log.error("使用者{}嘗試修改訂單{}",userId,order.getId());
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "無權限執行此操作"));
            }
            orderService.updateOrderCommentStatus(order,status);
            return ResponseEntity.ok(ApiResponse.success("評論狀態已修改為"+status,null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(400,e.getMessage()));
        }
    }


}
