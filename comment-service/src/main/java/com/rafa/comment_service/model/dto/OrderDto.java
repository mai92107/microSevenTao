package com.rafa.comment_service.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderDto {
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String roomName;
    private String name;

}
