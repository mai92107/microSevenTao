package com.example.user_service.model.dto;

import com.example.user_service.model.SEX;
import com.example.user_service.model.USER_ROLE;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserDto implements Serializable{

    private Long userId;
    private USER_ROLE ROLE;
    private String lastName;
    private String firstName;
    private String account;
    private String email;
    private String photo;
    private String nickName;
    private SEX sex;
    private String phoneNum;
    private String address;


}