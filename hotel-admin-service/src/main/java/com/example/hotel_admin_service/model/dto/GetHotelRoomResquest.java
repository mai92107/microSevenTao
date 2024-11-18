package com.example.hotel_admin_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetHotelRoomResquest {

    String hotelName;
    List<Long> roomIds;
}
