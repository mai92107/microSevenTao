package com.rafa.hotel_service.feign.feign;


import com.rafa.hotel_service.model.Comment;
import com.rafa.hotel_service.model.dto.CheckRoomAvailableByOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("COMMENT-SERVICE")//加入要映射的名稱（全大寫）
public interface CommentInterface {
    //加入要映射的方法

    @GetMapping("/hotel/comment/{hotelId}")
    public ResponseEntity<List<Comment>> getHotelComments(@PathVariable Long hotelId);
}
