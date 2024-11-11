package com.rafa.room_admin_service.service;

import com.rafa.room_admin_service.exception.RoomNotFoundException;
import com.rafa.room_admin_service.model.CreateRoomRequest;
import com.rafa.room_admin_service.model.Room;
import com.rafa.room_admin_service.model.dto.RoomDto;

import java.time.LocalDate;
import java.util.List;


public interface RoomService {
    public String createRooms(Long hotelId, List<CreateRoomRequest> requests);

    public boolean deleteRoomsByRoomIds(List<Long> roomIds);

    public Long findHotelIdByRoomId(Long roomId) throws RoomNotFoundException;

    public List<Long> getRoomIdsByHotelId(Long hotelId) throws RoomNotFoundException;

    public List<RoomDto> findRoomByHotelId(Long hotelId) throws RoomNotFoundException;

    public List<String> findRoomNamesByHotelId(Long hotelId) throws RoomNotFoundException;
}
