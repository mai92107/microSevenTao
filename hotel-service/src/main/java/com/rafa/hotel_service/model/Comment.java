package com.rafa.hotel_service.model;

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

	private int rate;

	@ManyToOne
	@JsonIgnore
	private Hotel hotel;

	private String userName;
	private String userPhoto;

	private long userId;
}
