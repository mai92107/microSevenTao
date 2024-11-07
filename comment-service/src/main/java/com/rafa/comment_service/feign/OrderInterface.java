package com.rafa.comment_service.feign;

import com.rafa.comment_service.model.dto.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("ORDER-USER-SERVICE")//加入要映射的名稱（全大寫）
public interface OrderInterface {
    //加入要映射的方法

    @GetMapping("/member/order/{orderId}")
    public ResponseEntity<OrderDto> getOrderData(@PathVariable Long orderId);

    @PutMapping("/member/order/{orderId}/commented/{status}")
    public ResponseEntity<String> updateOrderCommentStatus(@RequestHeader("Authorization") String jwt, @PathVariable Long orderId, @PathVariable Boolean status);

    }
