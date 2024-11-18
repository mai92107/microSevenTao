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
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


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
    RedisTemplate<String, Users> redisTemplate;


    private BoundValueOperations<String, Users> userValue(Long userId) {
        BoundValueOperations<String, Users> bvp = redisTemplate.boundValueOps("user:" + userId);
        bvp.expire(Duration.ofHours(1));
        return bvp;
    }

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

        userValue(userId).set(newUser);

        UserDto newUserDto = objectMapper.convertValue(newUser, UserDto.class);

        log.info("(adduser)新增使用者" + newUser);
        return newUserDto;
    }

    @Transactional
    @Override
    public UserDto findUserByUserId(Long userId) throws UserNotFoundException {
        Users user = userValue(userId).get();
        if (user != null) {
            log.info("(findUserByUserId)Redis找到使用者" + userId);
            return objectMapper.convertValue(user, UserDto.class);
        }
        user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("(findUserByUserId)查無使用者" + userId);
            throw new UserNotFoundException(userId);
        }
        log.info("(findUserByUserId)資料庫找到使用者" + user.getLastName() + "，開始存入redis");
        userValue(userId).set(user);
        UserDto userDto = objectMapper.convertValue(user, UserDto.class);
        return userDto;
    }

    @Override
    public UserDto setUserToHotelerFromUserId(Long userId) {
        try {
            UserDto userDto = findUserByUserId(userId);
            userDto.setROLE(USER_ROLE.ROLE_HOTELER);
            Users user = objectMapper.convertValue(userDto, Users.class);
            userRepository.save(user);
            userValue(userId).set(user);
            return userDto;
        } catch (UserNotFoundException e) {
            log.error("(setUserToHotelerFromUserId)" + e.getMessage());
            return null;
        }


    }

    @Override
    public UserDto updateUserData(Long userId, UpdateProfileRequest request) throws UserNotFoundException {
        UserDto userDto = findUserByUserId(userId);

        userDto.setAccount(request.getAccount());
        userDto.setPhoto(request.getPhoto());
        userDto.setFirstName(request.getFirstName());
        userDto.setNickName(request.getNickName());
        userDto.setSex(request.getSex());
        userDto.setAddress(request.getAddress());
        log.info("(updateUserData)修改使用者個人資料更新redis" + userDto);
        Users user = objectMapper.convertValue(userDto, Users.class);
        userRepository.save(user);
        userValue(userId).set(user);
        return userDto;
    }

}
