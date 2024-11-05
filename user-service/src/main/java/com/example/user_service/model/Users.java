package com.example.user_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Users {

    @Id
    private Long userId;
    private USER_ROLE ROLE;

    @Column(nullable = false)
    private String lastName;

    private String firstName;

    @Column(unique = true)
    private String account;

    @Column(unique = true)
    private String email;
    private String photo;
    private String nickName;
    private SEX sex;
    private String phoneNum;
    private String address;


}