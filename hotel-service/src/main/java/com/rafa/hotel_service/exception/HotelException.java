package com.rafa.hotel_service.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class HotelException extends Exception{


    String msg;
}
