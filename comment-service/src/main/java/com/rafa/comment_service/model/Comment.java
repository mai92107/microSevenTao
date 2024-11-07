package com.rafa.comment_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Comment {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long commentId;

	private String comment;
	private Integer rate;
	private Long hotelId;
	private Long roomId;
	private String userName;
	private String userPhoto;
	private Long userId;
	private Long orderId;
}
