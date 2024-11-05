package com.rafa.order_service.model.dto;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class CreateOrderRequest {
    private Long roomId;

    @Temporal(TemporalType.DATE)
    private LocalDate checkInDate;

    @Temporal(TemporalType.DATE)
    private LocalDate checkOutDate;

    private int totalPrice;
}
