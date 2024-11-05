package com.rafa.hotel_service.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class HotelEntity {
    //首頁用
    private List<HotelCardDto> hotels;
    private List<HotelCardDto> hotHotels;
    private List<HotelCardDto> newHotels;
    private List<HotelCardDto> bestHotels;
}
