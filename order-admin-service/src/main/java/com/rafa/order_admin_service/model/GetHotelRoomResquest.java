package com.rafa.order_admin_service.model;

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
