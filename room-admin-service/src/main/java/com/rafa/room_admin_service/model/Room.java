package com.rafa.room_admin_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Entity
@Data
public class Room {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long roomId;

	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> roomPic;

	private String roomName;

	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> specialties;

	@ElementCollection(fetch = FetchType.EAGER)
	private List<Integer> prices;

	private int roomSize;

	private int capacity;

	private Long hotelId;

}
