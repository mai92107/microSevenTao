package com.rafa.order_service.model.dto;

import com.rafa.order_service.model.SEX;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long userId;
    private String lastName;
    private String firstName;
    private String email;
    private String phoneNum;
    private String address;
    private SEX sex;

}