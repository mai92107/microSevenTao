package com.rafa.comment_service.service;

import com.rafa.comment_service.model.Comment;
import com.rafa.comment_service.model.dto.CommentDto;
import com.rafa.comment_service.model.dto.UserDto;

import java.util.List;

public interface CommentService {

    public void deleteComment(Long userId, Long commentId);

    public Comment addComment(UserDto user, Comment comment, Long hotelId);

    public List<CommentDto> allHotelComments(Long hotelId);

    public Double countHotelRate(Long hotelId);

    public Comment findCommentByCommentId(Long commentId);
}
