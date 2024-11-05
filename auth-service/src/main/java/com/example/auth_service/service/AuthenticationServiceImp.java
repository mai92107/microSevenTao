package com.example.auth_service.service;

import com.example.auth_service.model.USER_ROLE;
import com.example.auth_service.model.Users;
import com.example.auth_service.model.dto.LoginResponse;

import com.example.auth_service.model.dto.SignInRequest;
import com.example.auth_service.model.dto.SignUpRequest;
import com.example.auth_service.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLClientInfoException;

@Service
public class AuthenticationServiceImp implements AuthenticationService {

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    AuthRepository authRepository;

    @Autowired
    UserDetailService userDetailService;

    @Autowired
    AuthenticationManager authenticationManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Override
    public LoginResponse verifyUser(SignInRequest request) {

        UserDetails userDetails = userDetailService.loadUserByUsername(request.getUserName());

        if (userDetails == null) {
            System.out.println(request.getUserName() + "沒有註冊過");
            throw new UsernameNotFoundException("使用者帳號錯誤");
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUserName(),
                        request.getPassWord()
                )
        );
        if (!authentication.isAuthenticated()) {
            System.out.println(request.getUserName() + "沒有註冊過");
            throw new BadCredentialsException("使用者 " + request.getUserName() + " 密碼錯誤");
        }
        System.out.println("登入成功"+authentication.toString());
        USER_ROLE role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(USER_ROLE::valueOf)
                .findFirst()
                .orElse(USER_ROLE.ROLE_CUSTOMER);
        Users user = userDetailService.findUserByUserNameFromAccountOrEmail(request.getUserName());
        LoginResponse response = new LoginResponse();
        response.setJwt(jwtProvider.generateToken(authentication));
        response.setRole(role);
        response.setUsername(userDetails.getUsername());
        response.setUserId(user.getUserId());
        return response;
    }

    @Override
    public LoginResponse signUp(SignUpRequest request) throws Exception {

        Users userCheck = userDetailService.findUserByUserNameFromAccountOrEmail(request.getEmail());
        if (userCheck != null)
            throw new BadCredentialsException("此信箱已註冊");

        Users user = new Users(USER_ROLE.ROLE_CUSTOMER, encoder.encode(request.getPassword()), request.getEmail());
        user = authRepository.save(user);

        UserDetails realUser = userDetailService.loadUserByUsername(request.getEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword(), realUser.getAuthorities());

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getUserId());
        response.setJwt(jwtProvider.generateToken(authentication));
        response.setRole(user.getROLE());
        response.setUsername(user.getEmail());
        return response;
    }

    @Override
    public void updateAccount(Long userId, String account) {
        Users user = authRepository.findById(userId).orElseThrow(() -> new RuntimeException("查無此使用者"));
        System.out.println("找到使用者" + user.getUserId());
        user.setAccount(account);
        authRepository.save(user);
    }

    @Override
    public void updateRole(Long userId, USER_ROLE role) {
        Users user = authRepository.findById(userId).orElseThrow(() -> new RuntimeException("查無此使用者"));
        System.out.println("找到使用者" + user.getUserId());
        user.setROLE(role);
        authRepository.save(user);
    }
}
