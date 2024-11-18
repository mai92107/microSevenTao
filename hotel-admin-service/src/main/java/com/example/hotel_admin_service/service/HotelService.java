package com.example.hotel_admin_service.service;


import com.example.hotel_admin_service.exception.HotelNotFoundException;
import com.example.hotel_admin_service.model.Hotel;
import com.example.hotel_admin_service.model.dto.CreateHotelRequest;
import com.example.hotel_admin_service.model.dto.GetHotelRoomResquest;
import com.example.hotel_admin_service.model.dto.HotelCardDto;
import com.example.hotel_admin_service.model.dto.HotelDto;

import java.util.List;

public interface HotelService {

    public HotelDto createHotel(Long bossId, CreateHotelRequest request);

    public boolean deleteHotelByHotelId(Long hotelId) throws HotelNotFoundException;

    public List<HotelCardDto> findHotelsByBoss(Long bossId) throws HotelNotFoundException;

    public HotelDto updateHotelData(Long hotelId, CreateHotelRequest request) throws HotelNotFoundException;

    public HotelDto findHotelDtoByHotelId(Long hotelId) throws HotelNotFoundException;

    public Boolean validateBoss(Long userId, Long hotelId) throws HotelNotFoundException;

    public List<Long> findHotelIdsByBossId(Long userId);

    public String findHotelNameByHotelId(Long hotelId) throws HotelNotFoundException;

    public List<GetHotelRoomResquest> findHotelsWithRoomsByBoss(String jwt,Long userId) throws HotelNotFoundException;
}
