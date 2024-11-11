package com.rafa.hotel_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafa.hotel_service.feign.AuthInterface;
import com.rafa.hotel_service.model.dto.HotelCardDto;
import com.rafa.hotel_service.model.dto.HotelDetailDto;
import com.rafa.hotel_service.model.dto.HotelEntity;
import com.rafa.hotel_service.service.HotelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/hotel")
@Slf4j
public class HotelController {

    @Autowired
    HotelService hotelService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AuthInterface authInterface;

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDetailDto> findHotelByHotelId(@PathVariable Long hotelId) {
        try {
            HotelDetailDto hotel = hotelService.findHotelDtoByHotelId(hotelId);
            return new ResponseEntity<>(hotel, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/hotelAddress")
    public ResponseEntity<Set<Integer>> getHotelCities() {
        try {
            Set<Integer> cities = hotelService.getHotelCity();
            return new ResponseEntity<>(cities, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<String> updateHotelLikeList(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId) {
        try {
            Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody().getData(), Long.class);
            hotelService.updateHotelLikeList(userId, hotelId);
            return new ResponseEntity<>("已修改喜歡狀態", HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>("錯誤，請重新嘗試", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/userFavorites")
    public ResponseEntity<List<HotelCardDto>> getUserFavoriteHotels(@RequestHeader("Authorization") String jwt) {
        try {
            Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody().getData(), Long.class);
            List<HotelCardDto> favoriteHotels = hotelService.getFavoriteHotelsByUserId(userId);
            return new ResponseEntity<>(favoriteHotels, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{hotelId}/userFavorites")
    public ResponseEntity<Boolean> checkIsUserFavorite(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId) {
        try {
            Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody().getData(), Long.class);
            Boolean isFavorite = hotelService.checkIsFavorite(userId, hotelId);
            return new ResponseEntity<>(isFavorite, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/find")
    public ResponseEntity<HotelEntity> searchHotelByDetails(@RequestParam(value = "cityCode", required = false) Integer cityCode,
                                                            @RequestParam(value = "start", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
                                                            @RequestParam(value = "end", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end,
                                                            @RequestParam(value = "people", required = false) Integer people,
                                                            @RequestParam(value = "keyword", required = false) String keyword) {

        try {
            HotelEntity allHotelType = hotelService.searchAllHotelsWithSortedMethods(cityCode,keyword,start,end,people);
            return new ResponseEntity<>(allHotelType, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
