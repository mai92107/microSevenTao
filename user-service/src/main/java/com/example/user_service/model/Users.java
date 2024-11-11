package com.example.user_service.model;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
public class Users {

    @Id
    private Long userId;
    private USER_ROLE ROLE;
    private String account;

    @Column(nullable = false)
    private String lastName;
    private String firstName;
    private String nickName;

    @Column(unique = true)
    private String email;

    private String photo;
    private SEX sex;
    private String phoneNum;
    private String address;


}