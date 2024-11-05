package com.example.user_service.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class HotelDetailDto {
//旅店網頁用
        private Long hotelId;
        private String pictures;
        private String chName;
        private String enName;
        private String introduction;
        private double score;
}
