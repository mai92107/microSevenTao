package com.example.hotel_admin_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class Comment {

	private Long commentId;
	private String comment;
	private int rate;
	private Hotel hotel;
	private String userName;
	private String userPhoto;
	private long userId;
}
