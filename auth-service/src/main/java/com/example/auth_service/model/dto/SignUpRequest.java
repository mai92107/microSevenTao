package com.example.auth_service.model.dto;

import lombok.Data;

@Data
public class SignUpRequest {

	private String password;
	private String email;
	private String account;
}
