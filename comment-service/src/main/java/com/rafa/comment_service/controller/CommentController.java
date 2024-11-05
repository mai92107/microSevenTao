package com.rafa.comment_service.controller;

import com.rafa.comment_service.feign.AuthInterface;
import com.rafa.comment_service.feign.HotelInterface;
import com.rafa.comment_service.feign.UserInterface;
import com.rafa.comment_service.model.Comment;
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

    @PostMapping
    public ResponseEntity<Comment> addComment(@RequestHeader("Authorization") String jwt, @RequestBody Comment comment) {
        try {
            UserDto user = userInterface.getUserProfile(jwt).getBody();
            Comment newComment = commentService.addComment(user, comment);
            if (commentService.allHotelComments(comment.getHotelId()).size() >= 5) {
                Double rate = commentService.countHotelRate(comment.getHotelId());
                hotelInterface.updateHotelScore(jwt, comment.getHotelId(), rate);
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
            Long userId = authInterface.findUserIdByJwt(jwt).getBody();
            commentService.deleteComment(userId, commentId);
            return new ResponseEntity<>("刪除成功", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<List<Comment>> getHotelComments(@PathVariable Long hotelId) {
        try {
            System.out.println("我要搜尋這間hotel"+hotelId);
            List<Comment> comments = commentService.allHotelComments(hotelId);
            return new ResponseEntity<>(comments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}
