package com.example.auth_service.service;



import com.example.auth_service.model.USER_ROLE;
import com.example.auth_service.model.Users;
import com.example.auth_service.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailService implements UserDetailsService {

    @Autowired
    AuthRepository authRepository;



    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        Users user = findUserByUserNameFromAccountOrEmail(userName);

        if (user == null) {
            throw new UsernameNotFoundException("未找到使用者: " + userName);
        }

        USER_ROLE role = user.getROLE();
        return new User(user.getEmail(), user.getPassword(), List.of(new SimpleGrantedAuthority(role.toString())));
    }

    public Users findUserByUserNameFromAccountOrEmail(String userName) {
        if (userName.contains("@")) {
            return authRepository.findUserByEmail(userName);
        } else {
            return authRepository.findUserByAccount(userName);
        }
    }
}
