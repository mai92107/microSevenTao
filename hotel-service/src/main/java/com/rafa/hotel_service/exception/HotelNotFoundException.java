package com.rafa.hotel_service.exception;

public class HotelNotFoundException extends HotelException{
    public HotelNotFoundException(Long hotelId) {
        super("查無此旅店"+hotelId);
    }
}
