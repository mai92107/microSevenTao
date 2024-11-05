package com.example.hotel_admin_service.model.dto;

import com.example.hotel_admin_service.model.Address;
import com.example.hotel_admin_service.model.Comment;
import lombok.Data;

import java.util.List;

@Data
public class HotelCardDto {

    private Long hotelId;
    private String picture;
    private String chName;
    private String enName;
    private String introduction;
    private List<String> roomName;
    private double score;
}
