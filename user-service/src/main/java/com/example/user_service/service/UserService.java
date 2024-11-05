package com.example.user_service.service;

import com.example.user_service.model.Users;
import com.example.user_service.model.dto.HotelDetailDto;
import com.example.user_service.model.dto.SignUpRequest;
import com.example.user_service.model.dto.UpdateProfileRequest;

import java.util.List;

public interface UserService {

	public Users adduser(SignUpRequest request,Long userId);

	public Users findUserByUserId(Long userId);

	public void setUserToHotelerFromUserId(Long userId);

	public Users updateUserData(Long userId, UpdateProfileRequest request);

}
