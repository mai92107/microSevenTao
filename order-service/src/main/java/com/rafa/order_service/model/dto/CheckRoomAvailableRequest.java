package com.rafa.order_service.model.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CheckRoomAvailableRequest {

    private List<Long> roomIds;
    private LocalDate start;
    private LocalDate end;
}
