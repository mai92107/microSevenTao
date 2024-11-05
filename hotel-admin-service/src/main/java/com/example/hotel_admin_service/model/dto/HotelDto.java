package com.example.hotel_admin_service.model.dto;

import com.example.hotel_admin_service.model.Address;
import com.example.hotel_admin_service.model.Comment;
import lombok.Data;

import java.util.List;

@Data
public class HotelDto {

    private Long hotelId;
    private Long bossId;
    private List<String> pictures;
    private String chName;
    private String enName;
    private String introduction;
    private List<String> facilities;
    private Address address;
    private List<RoomDto> rooms;
    private List<Comment> comments;
    private double score;
}
