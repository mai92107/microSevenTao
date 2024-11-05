package com.rafa.order_service.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoomDto {


    private long roomId;
    private List<String> roomPic;
    private String roomName;

}
