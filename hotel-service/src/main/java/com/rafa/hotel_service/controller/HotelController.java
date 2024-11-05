package com.rafa.hotel_service.controller;

import com.rafa.hotel_service.feign.feign.AuthInterface;
import com.rafa.hotel_service.model.Hotel;
import com.rafa.hotel_service.model.dto.HotelCardDto;
import com.rafa.hotel_service.model.dto.HotelDetailDto;
import com.rafa.hotel_service.model.dto.HotelEntity;
import com.rafa.hotel_service.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/hotel")
public class HotelController {

    @Autowired
    HotelService hotelService;

    @Autowired
    AuthInterface authInterface;

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDetailDto> findHotelByHotelId(@PathVariable Long hotelId,
                                                             @RequestParam(value = "start", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
                                                             @RequestParam(value = "end", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end,
                                                             @RequestParam(value = "people", required = false) Integer people) {

        try {
            HotelDetailDto hotel = hotelService.findHotelDtoByHotelId(hotelId);
            System.out.println("搜尋這個hotel: " + hotel.getChName());

            if (start == null && end == null && people == null) {
                return new ResponseEntity<>(hotel, HttpStatus.OK);
            }
            HotelDetailDto detailHotel = hotelService.convertHotelFilterRoom(hotel, start, end, people);

            System.out.println("搜尋這間hotel許可的房間有幾間: " + detailHotel.getValidRooms().size());

            System.out.println("我查詢入住時間是：" + start + "離開時間是：" + end);


            return new ResponseEntity<>(detailHotel, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/hotelAddress")
    public ResponseEntity<Set<Integer>> getHotelCities() {
        try {
            Set<Integer> cities = hotelService.getHotelCity();
            return new ResponseEntity<>(cities, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<String> updateHotelLikeList(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId) {
        try {
            Long userId = authInterface.findUserIdByJwt(jwt).getBody();
            hotelService.updateHotelLikeList(userId, hotelId);
            return new ResponseEntity<>("已修改喜歡狀態", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("錯誤，請重新嘗試", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/userFavorites")
    public ResponseEntity<List<HotelCardDto>> getUserFavoriteHotels(@RequestHeader("Authorization")String jwt) {
        try {
            Long userId = authInterface.findUserIdByJwt(jwt).getBody();
            List<HotelCardDto> favoriteHotels = hotelService.getFavoriteHotelsByUserId(userId);
            return new ResponseEntity<>(favoriteHotels, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{hotelId}/userFavorites")
    public ResponseEntity<Boolean> checkIsUserFavorite(@RequestHeader("Authorization")String jwt, @PathVariable Long hotelId) {
        try {
            Long userId = authInterface.findUserIdByJwt(jwt).getBody();
            Boolean isFavorite = hotelService.checkIsFavorite(userId,hotelId);
            return new ResponseEntity<>(isFavorite, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/hotels")
    public ResponseEntity<HotelEntity> getHotels() {
        try{
            System.out.println("我要搜尋全部hotel");
            HotelEntity allHotelType = new HotelEntity();
            List<HotelCardDto> filteredHotel = hotelService.findALLHotelsByDetail(null, null,null,null,null);
            System.out.println("符合標準的hotel有幾間"+filteredHotel.size());
            allHotelType.setBestHotels(hotelService.sortHotelsByScore(filteredHotel));
            allHotelType.setHotHotels(hotelService.sortHotelsByOrders(filteredHotel));
            allHotelType.setNewHotels(hotelService.sortHotelsByBuildDate(filteredHotel));
            List<HotelCardDto> reversedList = new ArrayList<>(filteredHotel);
            Collections.reverse(reversedList);
            allHotelType.setHotels(reversedList);
            return new ResponseEntity<>(allHotelType, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/find")
    public ResponseEntity<HotelEntity> searchHotelByDetails(@RequestParam(value = "cityCode", required = false) Integer cityCode,
                                                            @RequestParam(value = "start",required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
                                                            @RequestParam(value = "end",required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end,
                                                            @RequestParam(value = "people", required = false) Integer people,
                                                            @RequestParam(value = "keyword", required = false) String keyword) {
        System.out.println("here!!!!");

        List<HotelCardDto> filteredHotel = hotelService.findALLHotelsByDetail(cityCode, keyword,start,end,people);

        HotelEntity allHotelType = new HotelEntity();
        allHotelType.setBestHotels(hotelService.sortHotelsByScore(filteredHotel));
        allHotelType.setHotHotels(hotelService.sortHotelsByOrders(filteredHotel));
        allHotelType.setNewHotels(hotelService.sortHotelsByBuildDate(filteredHotel));
        allHotelType.setHotels(filteredHotel);

        return new ResponseEntity<>(allHotelType, HttpStatus.OK);
    }



}
