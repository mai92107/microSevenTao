package com.rafa.order_service.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
public class RoomDto {


    private long roomId;
    private List<String> roomPic;
    private String roomName;


    @JsonIgnore
    private List<String> specialties;
    @JsonIgnore
    private Integer price;
    @JsonIgnore
    private int roomSize;
    @JsonIgnore
    private int capacity;
    @JsonIgnore
    private LocalDate start;
    @JsonIgnore
    private LocalDate end;


}
