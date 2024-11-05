package com.rafa.room_service.service;


import com.rafa.room_service.model.Room;
import com.rafa.room_service.model.roomDto.CreateRoomRequest;
import com.rafa.room_service.model.roomDto.RoomCardDto;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {

    public List<Room> findRoomByHotelId(Long hotelId);

    public List<LocalDate> separateLivingDays(LocalDate start, LocalDate end);

    public Integer countPriceByLivingDate(Long roomId, List<LocalDate> dates);

    public Integer countByDate(List<Integer> roomPrices, LocalDate date);

    public List<Long> filterInvalidRoomByDetails(List<Long> roomIds, Integer people);

    public RoomCardDto convertRoomToRoomCard(Long roomId, LocalDate start, LocalDate end);

    public List<Long> findRoomIdsByHotelId(Long hotelId);

    public List<String> findRoomNamesByRoomIds(List<Long> roomIds);

    public Integer getHotelMinPricePerDay(List<Long>roomIds,LocalDate start, LocalDate end);

    public Long findHotelIdByRoomId(Long roomId);
}
