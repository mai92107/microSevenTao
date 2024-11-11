package com.rafa.room_service;

import com.rafa.room_service.exception.RoomException;
import com.rafa.room_service.model.roomDto.RoomCardDto;
import com.rafa.room_service.service.RoomService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Slf4j
@Transactional
class RoomServiceTest {

    @Autowired
    RoomService roomService;


    @Test
    void getHotelMinPricePerDaySuccess() {
        log.info("22號旅店最低價為" + roomService.getHotelMinPricePerDay(
                List.of(352L, 353L, 356L, 402L, 403L),
                LocalDate.of(2024, 12, 15),
                LocalDate.of(2024, 12, 25)));
    }

    @Test
    void convertRoomToRoomCardError(){
        try {
            List<RoomCardDto> cards = roomService.convertRoomsToRoomCards(
                    List.of(352L, 353L, 356L, 402L, 403L),
                    LocalDate.of(2024, 12, 15),
                    LocalDate.of(2024, 12, 14));
        } catch (RoomException e) {
            log.error(e.getMsg());
        }
    }
    //查詢時間錯誤

    @Test
    void convertRoomToRoomCardSuccess() throws RoomException {
        log.info("獲得房間資料包含住宿價格" +
                roomService.convertRoomsToRoomCards(
                        List.of(352L, 353L, 356L, 402L, 403L),
                        LocalDate.of(2024, 12, 15),
                        LocalDate.of(2024, 12, 25)));

    }

}
