package com.example.auth_service.service;

import com.example.auth_service.exception.*;
import com.example.auth_service.model.USER_ROLE;
import com.example.auth_service.model.Users;
import com.example.auth_service.model.dto.LoginResponse;

import com.example.auth_service.model.dto.SignInRequest;
import com.example.auth_service.model.dto.SignUpRequest;
import com.example.auth_service.repository.AuthRepository;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
    public LoginResponse verifyUser(SignInRequest request) throws LoginErrorException {

        UserDetails userDetails = null;
        try{
            userDetails = userDetailService.loadUserByUsername(request.getUserName());
        }catch (Exception e){
            log.info(request.getUserName() + "沒有註冊過");
            throw new UsernameErrorException();
        }

        Authentication authentication =null;
        try{
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUserName(),
                            request.getPassWord()
                    )
            );
            log.info("登入成功"+authentication.toString());
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
        }catch(Exception e){
            log.info(e.getMessage());
            throw new PasswordErrorException();
        }
    }

    @Override
    public LoginResponse signUp(SignUpRequest request) throws DuplicateEmailException {

        Users userCheck = userDetailService.findUserByUserNameFromAccountOrEmail(request.getEmail());
        if (userCheck != null) {
            log.info("(updateAccount)此信箱已被註冊"+request.getEmail());
            throw new DuplicateEmailException();
        }

        Users user = new Users(USER_ROLE.ROLE_CUSTOMER, encoder.encode(request.getPassword()), request.getEmail());
        user = authRepository.save(user);

        UserDetails realUser = userDetailService.loadUserByUsername(request.getEmail());
        Authentication authentication =authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword(), realUser.getAuthorities()));
        log.info("註冊成功"+authentication.toString());

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getUserId());
        response.setJwt(jwtProvider.generateToken(authentication));
        response.setRole(user.getROLE());
        response.setUsername(user.getEmail());
        return response;
    }

    @Override
    public void updateAccount(Long userId, String account) throws DuplicateAccountException {
        Users fakeUser = authRepository.findUserByAccount(account);
        if(fakeUser!=null) {
            log.info("(updateAccount)此帳號已被使用"+account);
            throw new DuplicateAccountException(account);
        }
        Users user = authRepository.findById(userId).get();
        user.setAccount(account);
        log.info("已更新使用者" + user);
        authRepository.save(user);
    }

    @Override
    public void updateRole(Long userId, USER_ROLE role) throws RequestEmptyException {
        if(role==null||userId==null) {
            log.info("(updateRole)Error 修改資料不得為空");
            throw new RequestEmptyException();
        }
        Users user = authRepository.findById(userId).orElseThrow(() -> new RuntimeException("查無此使用者"));
        log.info("找到使用者" + user);
        user.setROLE(role);
        authRepository.save(user);
    }
}
