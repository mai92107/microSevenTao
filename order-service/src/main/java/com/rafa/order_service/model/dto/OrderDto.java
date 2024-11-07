package com.rafa.order_service.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rafa.order_service.model.STATUS;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderDto {

    private long id;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Long userId;

    private Long roomId;

    private String roomPic;

    private String roomName;

    private int totalPrice;

    private String name;

    private String phoneNum;

    private STATUS orderStatus= STATUS.PENDING;

    private Boolean commented = false;
}
