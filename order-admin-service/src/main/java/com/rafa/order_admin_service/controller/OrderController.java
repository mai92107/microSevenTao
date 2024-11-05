package com.rafa.order_admin_service.controller;

import com.rafa.order_admin_service.model.Orders;
import com.rafa.order_admin_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boss/orders")
public class OrderController {

    @Autowired
    OrderService orderService;

    @GetMapping
    public ResponseEntity<List<List<Orders>>> getHotelOrders(@RequestHeader("Authorization") String jwt, @RequestParam List<Long> roomIds) {
        try {

            List<Orders> allOrder = orderService.getOrdersByRoomList(roomIds);
            List<List<Orders>> allOrders = orderService.seperateStatus(allOrder);
            return new ResponseEntity<>(allOrders, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
    @PutMapping("/accept/{orderId}")
    public ResponseEntity<Orders> acceptOrder(@RequestHeader("Authorization") String jwt,@PathVariable Long orderId) {
        try {
            System.out.println("接收訂單");
            Orders order = orderService.acceptOrder(orderId);
            if (order != null)
                return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<Orders> acceptCancelOrder(@RequestHeader("Authorization") String jwt,@PathVariable Long orderId) {
        try {
            System.out.println("同意取消訂單");

            Orders order = orderService.acceptCancelOrder(orderId);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

}
