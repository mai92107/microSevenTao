package com.example.auth_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@NoArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private USER_ROLE ROLE;

    @Column(unique = true)
    private String account;

    private String password;

    @Column(unique = true)
    private String email;

    public Users(USER_ROLE ROLE, String password, String email) {
        this.ROLE = ROLE;
        this.password = password;
        this.email = email;
    }

}