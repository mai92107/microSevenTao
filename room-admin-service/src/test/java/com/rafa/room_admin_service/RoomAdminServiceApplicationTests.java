package com.rafa.room_admin_service;

import com.rafa.room_admin_service.exception.RoomNotFoundException;
import com.rafa.room_admin_service.model.CreateRoomRequest;
import com.rafa.room_admin_service.model.Room;
import com.rafa.room_admin_service.model.dto.RoomDto;
import com.rafa.room_admin_service.service.RoomService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
@Transactional
class RoomAdminServiceApplicationTests {


	@Autowired
	RoomService roomService;

//	@Test
//	void createRooms() {
//
//		log.info(roomService.createRooms(25L,
//				List.of(new CreateRoomRequest(
//						List.of("http://pictureOne","http://pictureTwo","http://pictureThree"),
//						"豪華單人床",List.of("高級馬桶","高級洗手台","高級浴缸"),
//						List.of(5000,5000,5000,5000,5000,5000,5000),
//						30,5))));
//	}

	@Test
	void deleteRooms() {

		log.info(roomService.deleteRoomsByRoomIds(List.of(3L,5L,7L,152L))+"");
	}

	@Test
	void findRoomByHotelId() throws RoomNotFoundException {

		log.info(roomService.findRoomByHotelId(22L)+"");
	}

	@Test
	void findRoomByHotelIdError() {

        try {
            List<RoomDto> room = (roomService.findRoomByHotelId(11L));
        } catch (RoomNotFoundException e) {
            log.error(e.getMsg());
        }
    }

}
