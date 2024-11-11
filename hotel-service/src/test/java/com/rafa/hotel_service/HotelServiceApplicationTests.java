package com.rafa.hotel_service;

import com.netflix.discovery.converters.Auto;
import com.rafa.hotel_service.exception.HotelNotFoundException;
import com.rafa.hotel_service.exception.SearchDataErrorException;
import com.rafa.hotel_service.model.Hotel;
import com.rafa.hotel_service.model.dto.HotelDetailDto;
import com.rafa.hotel_service.model.dto.HotelEntity;
import com.rafa.hotel_service.service.HotelService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
@Slf4j
@Transactional
class HotelServiceApplicationTests {

    @Autowired
    HotelService hotelService;

    @Test
    void findHotelDtoByHotelIdSuccess() throws HotelNotFoundException {
        log.info(hotelService.findHotelDtoByHotelId(22L) + "");
    }

    @Test
    void findHotelDtoByHotelIdError(){
        try {
            HotelDetailDto hotel = hotelService.findHotelDtoByHotelId(10L);
        } catch (HotelNotFoundException e) {
            log.error(e.getMsg());
        }
    }

    @Test
    void searchAllHotelsWithSortedMethods() throws SearchDataErrorException {
        log.info(hotelService.searchAllHotelsWithSortedMethods(null, null, null, null, null) + "");
    }

    @Test
    void searchAllHotelsWithSortedMethodsByDetail() throws SearchDataErrorException {
        log.info(hotelService.searchAllHotelsWithSortedMethods(
                0,
                null,
                LocalDate.of(2024, 12, 15),
                LocalDate.of(2024, 12, 20),
                4) + "");
    }

    @Test
    void searchAllHotelsWithSortedMethodsError() {
        try {
            HotelEntity hotels = hotelService.searchAllHotelsWithSortedMethods(
                    null,
                    null,
                    LocalDate.of(2024, 12, 15),
                    LocalDate.of(2024, 12, 16),
                    0);

        } catch (SearchDataErrorException e) {
            log.error(e.getMsg());
        }
    }

}
