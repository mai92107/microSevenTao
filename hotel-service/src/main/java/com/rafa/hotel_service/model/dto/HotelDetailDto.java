package com.rafa.hotel_service.model.dto;

import com.rafa.hotel_service.model.Address;
import lombok.Data;

import java.util.List;

@Data
public class HotelDetailDto {
//旅店網頁用
        private Long hotelId;
        private Long bossId;
        private List<String> pictures;
        private String chName;
        private String enName;
        private String introduction;
        private List<String> facilities;
        private Address address;
        private double score;
}
