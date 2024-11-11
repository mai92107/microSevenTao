package com.example.hotel_admin_service.repository;

import com.example.hotel_admin_service.model.Hotel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @EntityGraph(attributePaths = {"pictures"})
    public List<Hotel> findByBossId(Long bossId);


    @Query("SELECT h.hotelId FROM Hotel h WHERE h.bossId = :userId")
    public List<Long> findHotelIdsByBossId(@Param("userId") Long userId);

}
