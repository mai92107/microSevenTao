package com.rafa.hotel_service.service;

import com.rafa.hotel_service.exception.HotelNotFoundException;
import com.rafa.hotel_service.exception.SearchDataErrorException;
import com.rafa.hotel_service.model.Hotel;
import com.rafa.hotel_service.model.dto.HotelCardDto;
import com.rafa.hotel_service.model.dto.HotelDetailDto;
import com.rafa.hotel_service.model.dto.HotelEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface HotelService {

    public HotelDetailDto findHotelDtoByHotelId(Long hotelId) throws HotelNotFoundException;

    public Hotel updateHotelLikeList(Long userId, Long hotelId) throws HotelNotFoundException;

    public Set<Integer> getHotelCity();

    public List<HotelCardDto> getFavoriteHotelsByUserId(Long userId);

    Boolean checkIsFavorite(Long userId, Long hotelId);

    public HotelEntity searchAllHotelsWithSortedMethods(Integer cityCode, String keyword, LocalDate start, LocalDate end, Integer people) throws SearchDataErrorException;
}
