package com.rafa.comment_service.service;

import com.rafa.comment_service.model.Comment;
import com.rafa.comment_service.model.dto.UserDto;
import com.rafa.comment_service.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImp implements CommentService {

    @Autowired
    CommentRepository commentRepository;

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
    public Comment addComment(UserDto user, Comment comment) {

        if (comment.getHotelId() == null || comment.getComment() == null
                || comment.getRate() == null)
            throw new RuntimeException("評論內容不可為空" + comment);
        comment.setUserId(user.getUserId());
        String username = user.getNickName()!=null ? user.getNickName() : (user.getFirstName()!=null ? user.getFirstName() : user.getLastName());
        comment.setUserName(username);
        if (user.getPhoto()!=null)
            comment.setUserPhoto(user.getPhoto());
        return commentRepository.save(comment);

    }

    @Override
    public List<Comment> allHotelComments(Long hotelId) {
        return commentRepository.findByHotelId(hotelId);
    }

    @Override
    public Double countHotelRate(Long hotelId) {
        int rates = allHotelComments(hotelId).stream().mapToInt(Comment::getRate).sum();
        return (double)rates / allHotelComments(hotelId).size();
    }


}
