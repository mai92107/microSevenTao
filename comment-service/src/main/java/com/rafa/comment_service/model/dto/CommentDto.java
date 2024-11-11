package com.rafa.comment_service.model.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
public class CommentDto implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long commentId;

	private String comment;
	private Integer rate;
	private Long hotelId;
	private Long orderId;
	private Long roomId;
	private String userName;
	private String userPhoto;
	private Long userId;
private String livingTime;
private String roomType;
}
