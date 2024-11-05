package com.rafa.comment_service.repository;

import com.rafa.comment_service.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    public List<Comment> findByHotelId(Long hotelId);
}
