package com.rafa.room_service.model.roomDto;

import lombok.Data;

import java.util.List;

@Data
public class CreateRoomRequest {

	private List<String> roomPic;

	private String roomName;

	private List<String> specialties;

	private List<Integer> prices;

	private int capacity;

	private int roomSize;

}
