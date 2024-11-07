package com.rafa.hotel_service.service;

import com.rafa.hotel_service.model.Hotel;
import com.rafa.hotel_service.model.dto.HotelCardDto;
import com.rafa.hotel_service.model.dto.HotelDetailDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface HotelService {

    public HotelDetailDto findHotelDtoByHotelId(Long hotelId);

    public List<Hotel> getAllHotels();

    public List<HotelCardDto> sortHotelsByScore(List<HotelCardDto> hotels);

    public List<HotelCardDto> sortHotelsByOrders(List<HotelCardDto> hotels);

    public List<HotelCardDto> sortHotelsByBuildDate(List<HotelCardDto> hotels);

    public Hotel updateHotelLikeList(Long userId, Long hotelId);

    public Set<Integer> getHotelCity();

//    public HotelDetailDto convertHotelFilterRoom(HotelDetailDto dto, LocalDate start, LocalDate end, Integer people);

    public List<HotelCardDto> findALLHotelsByDetail(Integer cityCode, String keyword, LocalDate start, LocalDate end, Integer people);

    public List<HotelCardDto> getFavoriteHotelsByUserId(Long userId);

    Boolean checkIsFavorite(Long userId, Long hotelId);
}
