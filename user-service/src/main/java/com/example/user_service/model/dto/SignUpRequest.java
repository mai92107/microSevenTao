package com.example.user_service.model.dto;

import lombok.Data;

@Data
public class SignUpRequest {

	private String password;
	private String lastName;
	private String firstName;
	private String email;
	private String phoneNum;

}
