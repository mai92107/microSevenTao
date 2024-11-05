package com.rafa.hotel_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Data
public class CheckRoomAvailableByOrder {

    private List<Long> roomIds;
    private LocalDate start;
    private LocalDate end;
}
