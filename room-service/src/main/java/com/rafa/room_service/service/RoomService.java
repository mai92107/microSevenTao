package com.rafa.room_service.service;


import com.rafa.room_service.exception.LivingDateErrorException;
import com.rafa.room_service.exception.RoomException;
import com.rafa.room_service.exception.RoomNotFoundException;
import com.rafa.room_service.model.Room;
import com.rafa.room_service.model.roomDto.RoomCardDto;
import com.rafa.room_service.model.roomDto.RoomDto;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {

    public List<RoomDto> findRoomByHotelId(Long hotelId);

    public List<Long> filterInvalidRoomByDetails(List<Long> roomIds, Integer people);

    public List<RoomCardDto> convertRoomsToRoomCards(List<Long> roomIds, LocalDate start, LocalDate end) throws RoomException;

    public List<Long> findRoomIdsByHotelId(Long hotelId);

    public List<String> findRoomNamesByRoomIds(List<Long> roomIds);

    public Integer getHotelMinPricePerDay(List<Long> roomIds, LocalDate start, LocalDate end);

    public Long findHotelIdByRoomId(Long roomId);

    public List<RoomCardDto> getRoomCardsByDetailsFromRoomIds(Long hotelId, Integer people, LocalDate start, LocalDate end);
}
