package com.rafa.hotel_service.repository;

import com.rafa.hotel_service.model.Address;
import com.rafa.hotel_service.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query("SELECT h.address.city FROM Hotel h")
    public Set<String> findAllCityAddress();


    @Query("SELECT h FROM Hotel h " +
            "JOIN h.address a " +
            "WHERE (:cityCode IS NULL OR a.city = :cityCode) " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(h.chName) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            "OR LOWER(h.enName) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            "OR LOWER(h.introduction) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    public List<Hotel> findHotelsByDetail(@Param("cityCode") Integer cityCode, @Param("keyword") String keyword);


    @Query("SELECT h.hotelId FROM Hotel h WHERE :userId MEMBER OF h.likedByUsersIds")
    public Set<String> findFavoriteHotelIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT h.likedByUsersIds FROM Hotel h WHERE h.hotelId = :hotelId")
    public List<Long> getHotelFans(@Param("hotelId")Long hotelId);

    @Modifying
    @Query("UPDATE Hotel h SET h.score = :score WHERE h.hotelId = :hotelId")
    public int updateHotelScore(@Param("hotelId") Long hotelId,@Param("score") Double score);
}
