package com.example.user_service.model.dto;

import com.example.user_service.model.SEX;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    String account;
    String lastName;
    String firstName;
    String nickName;
    SEX sex;
    String phoneNum;
    String address;
    String photo;
}
