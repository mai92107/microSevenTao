package com.example.user_service.controller;

import com.example.user_service.exception.InvalidTokenException;
import com.example.user_service.exception.LoginErrorException;
import com.example.user_service.exception.RequestEmptyException;
import com.example.user_service.exception.SignupErrorException;
import com.example.user_service.feign.AuthInterface;
import com.example.user_service.model.USER_ROLE;
import com.example.user_service.model.Users;
import com.example.user_service.model.dto.LoginResponse;
import com.example.user_service.model.dto.SignInRequest;
import com.example.user_service.model.dto.SignUpRequest;
import com.example.user_service.model.dto.UpdateProfileRequest;
import com.example.user_service.response.ApiResponse;
import com.example.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@Slf4j
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AuthInterface authInterface;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Object>> userSignUp(@RequestBody SignUpRequest request) throws RequestEmptyException, SignupErrorException {

        if (request.getEmail() == null || request.getPhoneNum() == null || request.getLastName() == null || request.getPassword() == null)
            return ResponseEntity.ok(ApiResponse.error(400, "必要資料(信箱、密碼、電話、姓氏)不可空白"));
        ApiResponse res = objectMapper.convertValue(authInterface.signUp(request).getBody(), ApiResponse.class);
        if (res.getData() == null)
            return ResponseEntity.ok(ApiResponse.error(400, res.getMessage()));

        System.out.println(res.getData());
        LoginResponse response = objectMapper.convertValue(res.getData(),LoginResponse.class);
        userService.adduser(request, response.getUserId());

        return ResponseEntity.ok(ApiResponse.success("註冊成功", response));

    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<Object>> signIn(@RequestBody SignInRequest request) {

        System.out.println("準備登入" + request.getUserName());
        ApiResponse<Object> res = authInterface.signIn(request).getBody();
        if (res != null && res.getData() != null) {
            System.out.println(ApiResponse.success("登入成功", res.getData()));
            return ResponseEntity.ok(ApiResponse.success("登入成功", res.getData()));
        }

        System.out.println(ApiResponse.error(400, res.getMessage()));
        return ResponseEntity.ok(ApiResponse.error(400, res.getMessage()));

    }

    @GetMapping("/member")
    public ResponseEntity<ApiResponse<Object>> getUserProfile(@RequestHeader("Authorization") String jwt) throws InvalidTokenException, RequestEmptyException {
        Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody().getData(), Long.class);
        System.out.println("開始查使用者");
log.info("使用者Id: "+userId);
        Users user = userService.findUserByUserId(userId);
        System.out.println("搜到的人是" + user.getLastName());
        return ResponseEntity.ok(ApiResponse.success("資料取得成功", user));
    }

    ;

    @PutMapping("/member")
    public ResponseEntity<ApiResponse<Object>> updateUserData(@RequestHeader("Authorization") String jwt, @RequestBody UpdateProfileRequest request) throws InvalidTokenException, RequestEmptyException, SignupErrorException {
        Long userId = objectMapper.convertValue(authInterface.findUserIdByJwt(jwt).getBody().getData(), Long.class);
        Users user = userService.findUserByUserId(userId);
        if (!request.getAccount().trim().isEmpty()) {
            ApiResponse response = authInterface.updateAccount(jwt, request.getAccount()).getBody();
           log.info(response.getMessage());
            if(response!=null&&response.getStatus() == 400)
                return ResponseEntity.ok(ApiResponse.error(response.getStatus(), response.getMessage()));
        }
        Users updatedUser = userService.updateUserData(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", updatedUser));

    }

    @PutMapping("/member/hoteler")
    public ResponseEntity<ApiResponse<Object>> setUserToHoteler(@RequestHeader("Authorization") String jwt) throws InvalidTokenException, RequestEmptyException {

        Long userId = (Long) authInterface.findUserIdByJwt(jwt).getBody().getData();
        Users user = userService.findUserByUserId(userId);
        userService.setUserToHotelerFromUserId(user.getUserId());
        System.out.println(authInterface.updateRole(jwt, USER_ROLE.ROLE_HOTELER).getBody());

        return ResponseEntity.ok(ApiResponse.success("修改成功", null));

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
