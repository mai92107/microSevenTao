package com.rafa.room_admin_service.service;

import com.rafa.room_admin_service.model.CreateRoomRequest;
import com.rafa.room_admin_service.model.Room;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {
    public Room createRoom(Long hotelId, CreateRoomRequest request);

    public boolean deleteRoomByRoomId(Long roomId);

    public Long findHotelIdByRoomId(Long roomId);

    public List<Long> getRoomIdsByHotelId(Long hotelId);

    public List<Room> findRoomByHotelId(Long hotelId);

    public List<String> findRoomNamesByHotelId(Long hotelId);
}
