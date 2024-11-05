package com.example.hotel_admin_service.repository;

import com.example.hotel_admin_service.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address,Long> {
}
