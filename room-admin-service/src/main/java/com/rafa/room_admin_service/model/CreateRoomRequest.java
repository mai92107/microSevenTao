package com.rafa.room_admin_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {

	private List<String> roomPic;

	private String roomName;

	private List<String> specialties;

	private List<Integer> prices;

	private int capacity;

	private int roomSize;

}
