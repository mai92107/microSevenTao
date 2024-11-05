package com.rafa.hotel_service.model.dto;


import lombok.Data;

import java.util.List;

@Data
public class HotelCardDto {
    //首頁卡片用
    private Long hotelId;
    private String picture;
    private String chName;
    private String enName;
    private String introduction;
    private List<String> roomName;
    private Integer minPrice;
    private double score;
}