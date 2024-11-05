package com.example.auth_service.repository;

import com.example.auth_service.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRepository extends JpaRepository<Users,Long> {

    public Users findUserByEmail(String email);

    public Users findUserByAccount(String account);
}
