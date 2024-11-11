package com.example.hotel_admin_service.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HotelException extends Exception{


    String msg;
}
