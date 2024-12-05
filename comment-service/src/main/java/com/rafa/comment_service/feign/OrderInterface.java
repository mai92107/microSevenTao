package com.rafa.comment_service.feign;

import com.rafa.comment_service.model.dto.OrderDto;
import com.rafa.comment_service.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "order-user",url = "http://order-user.seventao:8080", fallback = OrderServiceFallback.class)//加入要映射的名稱（全大寫）
public interface OrderInterface {
    //加入要映射的方法

    @GetMapping("/order-user/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderData(@PathVariable Long orderId);

    @PutMapping("/order-user/{orderId}/commented/{status}")
    public ResponseEntity<ApiResponse<String>> updateOrderCommentStatus(@RequestHeader("Authorization") String jwt, @PathVariable Long orderId, @PathVariable Boolean status);

}

@Component
class OrderServiceFallback implements OrderInterface{

    @Override
    public ResponseEntity<ApiResponse<OrderDto>> getOrderData(Long orderId) {
        System.out.println("(OrderInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }

    @Override
    public ResponseEntity<ApiResponse<String>> updateOrderCommentStatus(String jwt, Long orderId, Boolean status) {
        System.out.println("(OrderInterface)無法解析");
        return ResponseEntity.ok(ApiResponse.error(404, null));
    }
}
