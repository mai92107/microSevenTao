package com.example.user_service.model.dto;

import com.example.user_service.model.SEX;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    String account;
    String firstName;
    String nickName;
    SEX sex;
    String address;
    String photo;
}
