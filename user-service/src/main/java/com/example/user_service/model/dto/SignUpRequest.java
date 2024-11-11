package com.example.user_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {

	private String password;
	private String lastName;
	private String firstName;
	private String email;
	private String phoneNum;

}
