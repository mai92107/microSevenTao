package com.rafa.room_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Entity
@Data
public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long roomId;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roomPic;

    private String roomName;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> specialties;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> prices;

    private int roomSize;

    private int capacity;

    private Long hotelId;

}
