package com.example.hotel_admin_service.exception;

public class HotelNotFoundException extends HotelException{
    public HotelNotFoundException(Long hotelId) {
        super("查無此旅店"+hotelId);
    }
    public HotelNotFoundException(Long userId,Long hotelId) {
        super("此使用者查無旅店"+userId);
    }
}
