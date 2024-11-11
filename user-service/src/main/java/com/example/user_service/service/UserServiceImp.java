package com.example.user_service.service;

import com.example.user_service.exception.RequestEmptyException;
import com.example.user_service.exception.UserNotFoundException;
import com.example.user_service.feign.AuthInterface;
import com.example.user_service.model.USER_ROLE;
import com.example.user_service.model.Users;
import com.example.user_service.model.dto.SignUpRequest;
import com.example.user_service.model.dto.UpdateProfileRequest;
import com.example.user_service.model.dto.UserDto;
import com.example.user_service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Slf4j
public class UserServiceImp implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthInterface authInterface;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CacheManager cacheManager;

    @Override
    public UserDto adduser(SignUpRequest request, Long userId) throws RequestEmptyException {
        if (userId == null) {
            log.info("(adduser)Error 未經驗證無法建立使用者建立失敗" + request);
            throw new RuntimeException("未經驗證無法建立");
        }
        if (request.getPhoneNum() == null || request.getLastName() == null) {
            log.info("(adduser)Error 必要內容如電話號碼及姓氏不可為空");
            throw new RequestEmptyException();
        }
        Users newUser = new Users();
        newUser.setUserId(userId);
        newUser.setLastName(request.getLastName());
        newUser.setFirstName(request.getFirstName());
        newUser.setEmail(request.getEmail());
        newUser.setPhoneNum(request.getPhoneNum());
        newUser.setROLE(USER_ROLE.ROLE_CUSTOMER);
        userRepository.save(newUser);

        UserDto newUserDto = objectMapper.convertValue(newUser, UserDto.class);
        Cache userCache = cacheManager.getCache("user");
        userCache.put(userId, newUserDto);
        log.info("(adduser)新增使用者" + newUser);
        return newUserDto;
    }

    @Override
    public UserDto findUserByUserId(Long userId) throws UserNotFoundException {
        Cache userCache = cacheManager.getCache("user");
        if (userCache.get(userId) != null) {
            log.info("(findUserByUserId)Redis找到使用者" + userId);
            return (UserDto) userCache.get(userId).get();
        }
        Users user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("(findUserByUserId)查無使用者" + userId);
            throw new UserNotFoundException(userId);
        }
        log.info("(findUserByUserId)資料庫找到使用者" + user.getLastName() + "，開始存入redis");
        UserDto userDto = objectMapper.convertValue(user, UserDto.class);
        userCache.put(userDto.getUserId(), userDto);
        return userDto;
    }

    @Override
    public UserDto setUserToHotelerFromUserId(Long userId) {
        UserDto user = null;
        try {
            user = findUserByUserId(userId);
        } catch (UserNotFoundException e) {
            log.error("(setUserToHotelerFromUserId)" + e.getMessage());
            return null;
        }
        user.setROLE(USER_ROLE.ROLE_HOTELER);
        userRepository.save(objectMapper.convertValue(user, Users.class));

        Cache userCache = cacheManager.getCache("user");
        userCache.put(userId, user);
        return user;
    }

    @Override
    public UserDto updateUserData(Long userId, UpdateProfileRequest request) throws UserNotFoundException {
        UserDto user = null;
        user = findUserByUserId(userId);

        user.setAccount(request.getAccount());
        user.setPhoto(request.getPhoto());
        user.setFirstName(request.getFirstName());
        user.setNickName(request.getNickName());
        user.setSex(request.getSex());
        user.setAddress(request.getAddress());
        Users userU = objectMapper.convertValue(user, Users.class);
        userRepository.save(userU);
        log.info("(updateUserData)修改使用者個人資料更新redis" + user);

        Cache userCache = cacheManager.getCache("user");
        userCache.put(userId, user);
        return user;
    }

}
