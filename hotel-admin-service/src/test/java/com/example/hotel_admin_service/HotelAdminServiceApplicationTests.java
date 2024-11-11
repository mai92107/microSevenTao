package com.example.hotel_admin_service;

import com.example.hotel_admin_service.exception.HotelNotFoundException;
import com.example.hotel_admin_service.model.Address;
import com.example.hotel_admin_service.model.dto.CreateHotelRequest;
import com.example.hotel_admin_service.service.HotelService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
@Transactional
class HotelAdminServiceApplicationTests {

	@Autowired
	HotelService hotelService;

	@Test
	void createHotel() {
		log.info(hotelService.createHotel(
				1L,
				new CreateHotelRequest(Map.of("firstPic","http://photo","secondPic","http://photo"),
						"新旅店","new hotel",
						"introductions for the hotel",
						List.of("近捷運","很大"),null,null)).toString());
	}

	@Test
	void deleteHotelByHotelId() throws HotelNotFoundException {
		log.info(hotelService.deleteHotelByHotelId(22L)+"");
	}

//	@Test
//	void findHotelsByBoss() throws HotelNotFoundException {
//		log.info(hotelService.findHotelsByBoss(1L)+"");
//	}

}
