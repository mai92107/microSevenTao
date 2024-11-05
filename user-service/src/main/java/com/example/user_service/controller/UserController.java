package com.example.user_service.controller;

import com.example.user_service.feign.AuthInterface;
import com.example.user_service.model.USER_ROLE;
import com.example.user_service.model.Users;
import com.example.user_service.model.dto.LoginResponse;
import com.example.user_service.model.dto.SignInRequest;
import com.example.user_service.model.dto.SignUpRequest;
import com.example.user_service.model.dto.UpdateProfileRequest;
import com.example.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AuthInterface authInterface;

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> userSignUp(@RequestBody SignUpRequest request) {
        try {
            if(request.getEmail()==null||request.getPhoneNum()==null||request.getLastName()==null||request.getPassword()==null)
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            LoginResponse res = authInterface.signUp(request).getBody();
            userService.adduser(request, res.getUserId());

            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<LoginResponse> signIn(@RequestBody SignInRequest request) {
        try {
            System.out.println("準備登入"+request.getUserName());
            LoginResponse res = authInterface.signIn(request).getBody();
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/member")
    public ResponseEntity<Users> getUserProfile(@RequestHeader("Authorization") String jwt) {
        try {
            Long userId = authInterface.findUserIdByJwt(jwt).getBody();
            System.out.println("開始查使用者");

            Users user = userService.findUserByUserId(userId);
            System.out.println("搜到的人是"+user.getLastName());
            return new ResponseEntity<>(user, HttpStatus.OK);

        } catch (RuntimeException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    };

    @PutMapping("/member")
    public ResponseEntity<Users> updateUserData(@RequestHeader("Authorization") String jwt, @RequestBody UpdateProfileRequest request) {
        try {
            Long userId = authInterface.findUserIdByJwt(jwt).getBody();
            Users user = userService.findUserByUserId(userId);
            if(request.getAccount()!=null) {
                authInterface.updateAccount(jwt, request.getAccount()).getStatusCode();
            }
            Users updatedUser = userService.updateUserData(user.getUserId(), request);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/member/hoteler")
    public ResponseEntity<String> setUserToHoteler(@RequestHeader("Authorization") String jwt) {
        try {
            Long userId = authInterface.findUserIdByJwt(jwt).getBody();
            Users user = userService.findUserByUserId(userId);
            userService.setUserToHotelerFromUserId(user.getUserId());
            System.out.println(authInterface.updateRole(jwt, USER_ROLE.ROLE_HOTELER).getBody());
        } catch (Exception e) {
            throw new RuntimeException("無法修改，請重新操作");
        }
        return new ResponseEntity<>("修改完成", HttpStatus.OK);

    }

//    @GetMapping("/{userId}")
//    public ResponseEntity<Users> getUserByUserId(@RequestHeader("Authorization") String jwt) {
//        try {
//            Long userId = authInterface.findUserIdByJwt(jwt).getBody();
//            Users user = userService.findUserByUserId(userId);
//            return new ResponseEntity<>(user, HttpStatus.OK);
//        } catch (Exception e) {
//            throw new RuntimeException("未找到該使用者");
//        }
//    }



}
