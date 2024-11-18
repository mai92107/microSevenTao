package com.rafa.hotel_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafa.hotel_service.exception.HotelNotFoundException;
import com.rafa.hotel_service.feign.AuthInterface;
import com.rafa.hotel_service.model.dto.HotelCardDto;
import com.rafa.hotel_service.model.dto.HotelDetailDto;
import com.rafa.hotel_service.model.dto.HotelEntity;
import com.rafa.hotel_service.response.ApiResponse;
import com.rafa.hotel_service.service.HotelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
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
    public ResponseEntity<ApiResponse<HotelDetailDto>> findHotelByHotelId(@PathVariable Long hotelId) {
        try {
            HotelDetailDto hotel = hotelService.findHotelDtoByHotelId(hotelId);
            return ResponseEntity.ok(ApiResponse.success("成功找到旅店", hotel));
        } catch (HotelNotFoundException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMsg()));
        } catch (Exception e) {
            log.error("(findHotelByHotelId)" + e.getMessage());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "尋找旅店發生錯誤"));
    }

    @GetMapping("/hotelAddress")
    public ResponseEntity<ApiResponse<Set<String>>> getHotelCities() {
        try {
            Set<String> cities = hotelService.getHotelCity();
            return ResponseEntity.ok(ApiResponse.success("找到城市" + cities.size() + "筆", cities));

        } catch (Exception e) {
            e.printStackTrace();
            log.error("(getHotelCities)" + e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<ApiResponse<String>> updateHotelLikeList(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId) {
        try {
            Long userId = authInterface.findUserIdByJwt(jwt).getBody().getData();
            hotelService.updateHotelLikeList(userId, hotelId);
            return ResponseEntity.ok(ApiResponse.success("成功更新喜歡的旅店", null));
        } catch (HotelNotFoundException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMsg()));

        } catch (Exception e) {
            log.error("(updateHotelLikeList)" + e.getMessage());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "更新失敗"));
    }

    @GetMapping("/userFavorites")
    public ResponseEntity<ApiResponse<List<HotelCardDto>>> getUserFavoriteHotels(@RequestHeader("Authorization") String jwt) {
        try {
            Long userId = authInterface.findUserIdByJwt(jwt).getBody().getData();
            List<HotelCardDto> favoriteHotels = hotelService.getFavoriteHotelsByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success("成功取得使用者喜愛清單", favoriteHotels));
        } catch (Exception e) {
            log.error("(getUserFavoriteHotels)" + e.getMessage());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "無法取得使用者喜愛清單"));
    }

    @GetMapping("/{hotelId}/userFavorites")
    public ResponseEntity<ApiResponse<Boolean>> checkIsUserFavorite(@RequestHeader("Authorization") String jwt, @PathVariable Long hotelId) {
        try {
            Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody().getData(), Long.class);
            Boolean isFavorite = hotelService.checkIsFavorite(userId, hotelId);
            return ResponseEntity.ok(ApiResponse.success("成功確認使用者喜好", isFavorite));
        } catch (Exception e) {
            log.error("(checkIsUserFavorite)"+e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/find")
    public ResponseEntity<ApiResponse<HotelEntity>> searchHotelByDetails(@RequestParam(value = "cityCode", required = false) Integer cityCode,
                                                            @RequestParam(value = "start", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
                                                            @RequestParam(value = "end", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end,
                                                            @RequestParam(value = "people", required = false) Integer people,
                                                            @RequestParam(value = "keyword", required = false) String keyword) {

        try {
            HotelEntity allHotelType = hotelService.searchAllHotelsWithSortedMethods(cityCode, keyword, start, end, people);
            return ResponseEntity.ok(ApiResponse.success("成功取得查詢的旅店",allHotelType));
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400,"取得旅店發生錯誤"));
    }
}
