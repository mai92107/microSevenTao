package com.example.user_service.service;

import com.example.user_service.exception.RequestEmptyException;
import com.example.user_service.exception.UserNotFoundException;
import com.example.user_service.model.Users;
import com.example.user_service.model.dto.HotelDetailDto;
import com.example.user_service.model.dto.SignUpRequest;
import com.example.user_service.model.dto.UpdateProfileRequest;
import com.example.user_service.model.dto.UserDto;

import java.util.List;

public interface UserService {

	public UserDto adduser(SignUpRequest request,Long userId) throws RequestEmptyException;

	public UserDto findUserByUserId(Long userId) throws UserNotFoundException;

	public UserDto setUserToHotelerFromUserId(Long userId);

	public UserDto updateUserData(Long userId, UpdateProfileRequest request) throws UserNotFoundException;

}
