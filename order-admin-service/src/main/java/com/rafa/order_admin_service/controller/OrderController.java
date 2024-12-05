package com.rafa.order_admin_service.controller;

import com.rafa.order_admin_service.feign.AuthInterface;
import com.rafa.order_admin_service.feign.HotelInterface;
import com.rafa.order_admin_service.feign.RoomInterface;
import com.rafa.order_admin_service.model.Orders;
import com.rafa.order_admin_service.response.ApiResponse;
import com.rafa.order_admin_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order-admin")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    HotelInterface hotelInterface;

    @Autowired
    AuthInterface authInterface;

    @Autowired
    RoomInterface roomInterface;

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<List<Orders>>>> getHotelOrders(@RequestHeader("Authorization") String jwt, @RequestParam List<Long> roomIds) {
        try {
            List<Orders> allOrder = orderService.getOrdersByRoomLists(roomIds);
            List<List<Orders>> allOrders = orderService.seperateStatus(allOrder);
            return ResponseEntity.ok(ApiResponse.success("成功獲取所有旅店訂單", allOrders));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "獲取失敗，重新嘗試" + e.getMessage()));
        }
    }

    @PutMapping("/accept/{orderId}")
    public ResponseEntity<ApiResponse<Orders>> acceptOrder(@RequestHeader("Authorization") String jwt, @PathVariable Long orderId) {
        try {
            System.out.println("接收訂單");
            Orders order = orderService.acceptOrder(orderId);
            if (order != null)
                return ResponseEntity.ok(ApiResponse.success("成功接單", order));
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "查無此訂單"));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "操作失敗，重新嘗試" + e.getMessage()));
        }
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<ApiResponse<Orders>> acceptCancelOrder(@RequestHeader("Authorization") String jwt, @PathVariable Long orderId) {
        try {
            System.out.println("同意取消訂單");
            Orders order = orderService.acceptCancelOrder(orderId);
            return ResponseEntity.ok(ApiResponse.success("成功取消訂單", order));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "操作失敗，重新嘗試" + e.getMessage()));
        }
    }

}
