package com.example.hotel_admin_service.service;


import com.example.hotel_admin_service.model.Hotel;
import com.example.hotel_admin_service.model.dto.CreateHotelRequest;
import com.example.hotel_admin_service.model.dto.HotelCardDto;
import com.example.hotel_admin_service.model.dto.HotelDto;

import java.util.List;

public interface HotelService {

    public Hotel createHotel(Long bossId, CreateHotelRequest request);

    public boolean deleteHotelByHotelId(Long hotelId);

    public List<HotelCardDto> findHotelsByBoss(Long bossId);

    public Hotel findHotelByHotelId(Long hotelId);

    public Hotel updateHotelData(Long hotelId, CreateHotelRequest request);

    public HotelDto findHotelDtoByHotelId(Long hotelId);

    public void updateHotelScore(Long hotelId, Double score);

    public Boolean validateBoss(Long userId, Long hotelId);

    public List<Long> findHotelIdsByBossId(Long userId);
}
