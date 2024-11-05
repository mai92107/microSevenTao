package com.example.hotel_admin_service.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class RoomDto {
    private Long hotelId;
    private long roomId;
    private List<String> roomPic;
    private String roomName;
    private List<String> specialties;
    private List<Integer> prices;
    private int roomSize;
    private int capacity;
}
