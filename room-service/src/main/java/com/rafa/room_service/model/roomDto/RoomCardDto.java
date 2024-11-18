package com.rafa.room_service.model.roomDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomCardDto {

    private long roomId;

    private List<String> roomPic;

    private String roomName;

    private List<String> specialties;

    private Integer price;

    private int roomSize;

    private int capacity;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate start;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate end;
}
