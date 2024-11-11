package com.rafa.room_service.repository;

import com.rafa.room_service.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    public List<Room> findByHotelId(Long hotelId);

    @Query("SELECT r.roomId FROM Room r WHERE hotelId = :hotelId")
    public List<Long> findRoomIdsByHotelId(@Param("hotelId") Long hotelId);


    @Query("SELECT r.hotelId FROM Room r WHERE roomId = :roomId")
    public Long findHotelIdByRoomId(Long roomId);
}
