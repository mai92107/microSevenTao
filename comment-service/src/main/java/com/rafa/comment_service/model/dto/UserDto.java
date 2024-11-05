package com.rafa.comment_service.model.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class UserDto {

    private Long userId;
    private String lastName;
    private String firstName;
    private String photo;
    private String nickName;

}