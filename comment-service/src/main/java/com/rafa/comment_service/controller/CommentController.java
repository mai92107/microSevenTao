package com.rafa.comment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafa.comment_service.feign.*;
import com.rafa.comment_service.model.Comment;
import com.rafa.comment_service.model.dto.CommentDto;
import com.rafa.comment_service.model.dto.UserDto;
import com.rafa.comment_service.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/hotel/comment")
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
    ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<Comment> addComment(@RequestHeader("Authorization") String jwt, @RequestBody Comment comment) {
        try {
            UserDto user = objectMapper.convertValue(userInterface.getUserProfile(jwt).getBody().getData(), UserDto.class);
            Long hotelId = roomInterface.findHotelIdByRoomId(comment.getRoomId()).getBody();
            orderInterface.updateOrderCommentStatus(jwt, comment.getOrderId(), true);
            Comment newComment = commentService.addComment(user, comment, hotelId);
            if (commentService.allHotelComments(hotelId).size() >= 5) {
                Double rate = commentService.countHotelRate(hotelId);
                try {
                    hotelInterface.updateHotelScore(jwt, hotelId, rate);
                } catch (Exception e) {
                    System.err.println("Failed to update hotel score: " + e.getMessage());
                }
            }
            return new ResponseEntity<>(newComment, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@RequestHeader("Authorization") String jwt, @PathVariable Long commentId) {
        try {
            Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody().getData(), Long.class);
            CommentDto comment = commentService.findCommentByCommentId(commentId);
            if (comment != null)
                orderInterface.updateOrderCommentStatus(jwt, comment.getOrderId(), false);
            commentService.deleteComment(userId, commentId);
            return new ResponseEntity<>("刪除成功", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<List<CommentDto>> getHotelComments(@PathVariable Long hotelId) {
        try {
            System.out.println("我要搜尋這間hotel" + hotelId);
            List<CommentDto> comments = commentService.allHotelComments(hotelId);
            return new ResponseEntity<>(comments, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}
