package com.example.user_service.service;

import com.example.user_service.feign.AuthInterface;
import com.example.user_service.model.USER_ROLE;
import com.example.user_service.model.Users;
import com.example.user_service.model.dto.HotelDetailDto;
import com.example.user_service.model.dto.SignUpRequest;
import com.example.user_service.model.dto.UpdateProfileRequest;
import com.example.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImp implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthInterface authInterface;

    @Override
    public Users adduser(SignUpRequest request,Long userId) {

        Users newUser = new Users();
        newUser.setUserId(userId);
        newUser.setLastName(request.getLastName());
        newUser.setFirstName(request.getFirstName());
        newUser.setEmail(request.getEmail());
        newUser.setPhoneNum(request.getPhoneNum());
        newUser.setROLE(USER_ROLE.ROLE_CUSTOMER);

        userRepository.save(newUser);
        System.out.println(newUser.toString());
        return newUser;
    }

    @Override
    public Users findUserByUserId(Long userId) {
        Users user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            throw new NullPointerException();
        }
        return user;
    }

    @Override
    public void setUserToHotelerFromUserId(Long userId) {
        Users user = findUserByUserId(userId);
        user.setROLE(USER_ROLE.ROLE_HOTELER);
        userRepository.save(user);
    }

    @Override
    public Users updateUserData(Long userId, UpdateProfileRequest request) {
        Users user = findUserByUserId(userId);
        if (request.getAccount() != null) {
            user.setAccount(request.getAccount());
        }
        user.setLastName(request.getLastName());
        if (request.getPhoto() != null)
            user.setPhoto(request.getPhoto());
        if (request.getFirstName() != null)
            user.setFirstName(request.getFirstName());
        if (request.getNickName() != null)
            user.setNickName(request.getNickName());
        if (request.getSex() != null)
            user.setSex(request.getSex());
        if (request.getPhoneNum() != null)
            user.setPhoneNum(request.getPhoneNum());
        if (request.getAddress() != null)
            user.setAddress(request.getAddress());
        return userRepository.save(user);
    }

}
