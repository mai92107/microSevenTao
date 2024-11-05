package com.example.hotel_admin_service.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderDto {

    private long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    private Long userId;

    private Long roomId;

    private String roomPic;

    private String roomName;

    private int totalPrice;

    private String name;

    private String phoneNum;

    private String orderStatus;
}
