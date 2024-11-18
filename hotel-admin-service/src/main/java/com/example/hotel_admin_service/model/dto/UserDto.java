package com.example.hotel_admin_service.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long userId;
    @JsonIgnore
    private String firstName;
    private String lastName;
    @JsonIgnore
    private String account;
    @JsonIgnore
    private String photo;
    @JsonIgnore
    private String nickName;
    private String sex;
    private String email;
    private String phoneNum;
    private String address;
    private String ROLE;

}