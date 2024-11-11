package com.rafa.room_service.model.roomDto;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RoomDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long roomId;

    private List<String> roomPic;

    private String roomName;

    private List<String> specialties;

    private List<Integer> prices;

    private int roomSize;

    private int capacity;

    private Long hotelId;

}
