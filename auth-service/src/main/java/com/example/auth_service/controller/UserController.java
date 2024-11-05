package com.example.auth_service.controller;


import com.example.auth_service.model.USER_ROLE;
import com.example.auth_service.model.Users;
import com.example.auth_service.model.dto.LoginResponse;
import com.example.auth_service.model.dto.SignInRequest;
import com.example.auth_service.model.dto.SignUpRequest;
import com.example.auth_service.service.AuthenticationService;
import com.example.auth_service.service.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    Environment environment;

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateJwt(@RequestHeader("Authorization") String jwt) {
        if (jwt.isEmpty())
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        Boolean isSuccess = jwtProvider.validateJwt(jwt);
        return new ResponseEntity<>(isSuccess, HttpStatus.OK);
    }

    @GetMapping("/findUser")
    public ResponseEntity<Long> findUserIdByJwt(@RequestHeader("Authorization") String jwt) {
        System.out.println("using port : "+environment.getProperty("local.server.port"));

        try {
            if (jwt.isEmpty())
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            if (!jwtProvider.validateJwt(jwt))
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

            Long userId = jwtProvider.findUserIdByJwt(jwt);
            return new ResponseEntity<>(userId, HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

    //
    @PostMapping("/signIn")
    public ResponseEntity<LoginResponse> signIn(@RequestBody SignInRequest request) {
        try{
            System.out.println("我要登入" + request.getUserName());

            LoginResponse res = authenticationService.verifyUser(request);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        }
    }

    @PostMapping("/signUp")
    public ResponseEntity<LoginResponse> signUp(@RequestBody SignUpRequest request) {
        System.out.println(request);
        if (request == null)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        try {
            LoginResponse res = authenticationService.signUp(request);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/member/account")
    public ResponseEntity<String> updateAccount(@RequestHeader("Authorization") String jwt, @RequestBody String account) {
        try {
            System.out.println("我是" + account);

            Long userId = jwtProvider.findUserIdByJwt(jwt);
            System.out.println("我的userID" + userId);

            if (userId == null)
                return new ResponseEntity<>("驗證失敗", HttpStatus.BAD_REQUEST);
            System.out.println("到這了嗎？？");
            authenticationService.updateAccount(userId, account);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("修改發生錯誤", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("設置完成", HttpStatus.OK);

    }

    @PutMapping("/member/role")
    public ResponseEntity<String> updateRole(@RequestHeader("Authorization") String jwt, @RequestBody USER_ROLE role) {
        try {
            Long userId = jwtProvider.findUserIdByJwt(jwt);
            System.out.println("我的userID" + userId);
            if (userId == null)
                return new ResponseEntity<>("驗證失敗", HttpStatus.BAD_REQUEST);
            authenticationService.updateRole(userId, role);
            System.out.println("認證中心修改完成");
        } catch (RuntimeException e) {
            return new ResponseEntity<>("修改發生錯誤", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("設置完成", HttpStatus.OK);

    }
}
