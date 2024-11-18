package com.rafa.hotel_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelEntity {
    //首頁用
    private List<HotelCardDto> hotels;
    private List<HotelCardDto> hotHotels;
    private List<HotelCardDto> newHotels;
    private List<HotelCardDto> bestHotels;
}
