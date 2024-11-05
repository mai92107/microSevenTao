package com.example.hotel_admin_service.model.dto;

import com.example.hotel_admin_service.model.Address;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CreateHotelRequest {
	private Map<String, String> pictures;
	private String chName;
	private String enName;
	private String introduction;
	private List<String> facilities;
	private Address address;
 private List<Long> roomIds;
}
