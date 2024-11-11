package com.example.hotel_admin_service.model.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.io.Serializable;


@Data
public class AddressDto implements Serializable {
	private Long addressId;
	private int city;
	private int district;
	private String street;
	private String number;
}
