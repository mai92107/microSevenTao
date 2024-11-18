package com.rafa.comment_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafa.comment_service.feign.OrderInterface;
import com.rafa.comment_service.model.Comment;
import com.rafa.comment_service.model.dto.CommentDto;
import com.rafa.comment_service.model.dto.OrderDto;
import com.rafa.comment_service.model.dto.UserDto;
import com.rafa.comment_service.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImp implements CommentService {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    OrderInterface orderInterface;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public void deleteComment(Long userId, Long commentId) {

        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null)
            throw new RuntimeException("查無此評論Id" + commentId);
        if(!comment.getUserId().equals(userId) )
            throw  new RuntimeException("請勿刪除別人留言");
        commentRepository.deleteById(commentId);
    }

    @Override
    public Comment addComment(UserDto user, Comment comment, Long hotelId) {

        if (comment.getComment() == null || comment.getRate() == null || hotelId==null)
            throw new RuntimeException("評論必要內容不可為空" + comment);
        comment.setHotelId(hotelId);
        comment.setUserId(user.getUserId());
        String username = user.getNickName()!=null ? user.getNickName() : (user.getFirstName()!=null ? user.getFirstName() : user.getLastName());
        comment.setUserName(username);
        if (user.getPhoto()!=null)
            comment.setUserPhoto(user.getPhoto());

        return commentRepository.save(comment);

    }

    @Override
    public List<CommentDto> allHotelComments(Long hotelId) {
        List<Comment> comments = commentRepository.findByHotelId(hotelId);
        List<CommentDto> hotelComments = comments.stream().map(comment -> {
            CommentDto hotelComment = objectMapper.convertValue(comment, CommentDto.class);
            if(comment.getOrderId()!=null) {
                OrderDto order = orderInterface.getOrderData(comment.getOrderId()).getBody().getData();
                hotelComment.setRoomType(order.getRoomName());
                hotelComment.setLivingTime(order.getCheckInDate() + " ~ " + order.getCheckOutDate());
            }
            return hotelComment;
        }).toList();
        return hotelComments;
    }

    @Override
    public Double countHotelRate(Long hotelId) {
        int rates = allHotelComments(hotelId).stream().mapToInt(CommentDto::getRate).sum();
        return (double)rates / allHotelComments(hotelId).size();
    }

    @Override
    public CommentDto findCommentByCommentId(Long commentId) {
        return objectMapper.convertValue(commentRepository.findById(commentId).orElse(null), CommentDto.class);
    }


}
