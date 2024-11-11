package com.example.hotel_admin_service.model.dto;

import com.example.hotel_admin_service.model.Address;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class HotelDto {
    private Long hotelId;
    private Long bossId;
    private List<String> pictures;
    private String chName;
    private String enName;
    private String introduction;
    private List<String> facilities;
    private Address address;
    @JsonIgnore
    private LocalDateTime buildDate;
    @JsonIgnore
    private List<Long> likedByUsersIds;
    private double score;
    private List<Double> location;

}
