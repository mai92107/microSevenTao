package com.rafa.comment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafa.comment_service.feign.*;
import com.rafa.comment_service.model.Comment;
import com.rafa.comment_service.model.dto.CommentDto;
import com.rafa.comment_service.model.dto.CustomRabbitMessage;
import com.rafa.comment_service.model.dto.UserDto;
import com.rafa.comment_service.rabbitMessagePublisher.SyncHotelPublish;
import com.rafa.comment_service.response.ApiResponse;
import com.rafa.comment_service.service.CommentService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;


@RestController
@RequestMapping("/hotel/comment")
@Slf4j
public class CommentController {

    @Autowired
    CommentService commentService;

    @Autowired
    UserInterface userInterface;

    @Autowired
    HotelInterface hotelInterface;

    @Autowired
    AuthInterface authInterface;

    @Autowired
    OrderInterface orderInterface;

    @Autowired
    RoomInterface roomInterface;

    @Autowired
    SyncHotelPublish syncHotelPublish;

    @Autowired
    ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<Comment>> addComment(@RequestHeader("Authorization") String jwt, @RequestBody Comment comment) {
        try {
            UserDto user = userInterface.getUserProfile(jwt).getBody().getData();
            Long hotelId = roomInterface.findHotelIdByRoomId(comment.getRoomId()).getBody().getData();

            Comment newComment = commentService.addComment(user, comment, hotelId);
            orderInterface.updateOrderCommentStatus(jwt, comment.getOrderId(), true);
            if (commentService.allHotelComments(hotelId).size() >= 5) {
                Double rate = commentService.countHotelRate(hotelId);
                try {
                    log.info("達到標準，準備送出分數{}請求",rate);
                    syncHotelPublish.sendMsg(new CustomRabbitMessage("updateAdminHotelScoreRoute", "commentExchange", "hotel:"+hotelId+",Score:"+rate));
                    syncHotelPublish.sendMsg(new CustomRabbitMessage("updateUserHotelScoreRoute", "commentExchange", "hotel:"+hotelId+",Score:"+rate));
                } catch (Exception e) {
                    log.error("(addComment) 發送 RabbitMQ 消息失敗: {}", e.getMessage());
                }
            }
            return ResponseEntity.ok(ApiResponse.success("留言新增成功", newComment));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }


    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(@RequestHeader("Authorization") String jwt, @PathVariable Long commentId) {
        try {
            Long userId = authInterface.findUserIdByJwt(jwt).getBody().getData();
            CommentDto comment = commentService.findCommentByCommentId(commentId);
            if (comment.getUserId() != userId)
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "無權限執行此操作"));
            log.info(orderInterface.updateOrderCommentStatus(jwt, comment.getOrderId(), false).getBody().getMessage());
            commentService.deleteComment(userId, commentId);
            return ResponseEntity.ok(ApiResponse.success("評論刪除成功",null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400,"評論刪除失敗，請重新嘗試"));
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getHotelComments(@PathVariable Long hotelId) {
        try {
            System.out.println("我要搜尋這間hotel" + hotelId);
            List<CommentDto> comments = commentService.allHotelComments(hotelId);
            return ResponseEntity.ok(ApiResponse.success("評論取得成功",comments));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400,"評論存取失敗"));
    }
}
