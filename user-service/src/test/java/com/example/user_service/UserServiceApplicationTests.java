package com.example.user_service;

import com.example.user_service.exception.RequestEmptyException;
import com.example.user_service.exception.UserNotFoundException;
import com.example.user_service.model.dto.SignUpRequest;
import com.example.user_service.model.dto.UpdateProfileRequest;
import com.example.user_service.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
@Slf4j
class UserServiceApplicationTests {

    @Autowired
    UserService userService;

    @Test
    void addUserSuccess() throws RequestEmptyException {
        userService.adduser(
                new SignUpRequest(
                        null, "Mai",
                        "BinZhen", "navalBoy@navy",
                        "0900000000"),
                50L);
    }

    @Test
    void addUserError() {
        try {
            userService.adduser(
                    new SignUpRequest(
                            null, null,
                            "BinZhen", "navalBoy@navy",
                            null),
                    50L);
        } catch (RequestEmptyException e) {
            log.error(e.getMsg());
        }
    }
    //缺少必要內容＊姓氏＊手機號碼

    @Test
    void updateUserSuccess() throws UserNotFoundException {
        userService.updateUserData(3L,
                new UpdateProfileRequest( "account",
                        "BinZhen",null,null,
                        "台北路一號",null));
    }

    @Test
    void updateUserError() {
        try {
            userService.updateUserData(50L,
                    new UpdateProfileRequest("account",
                            "BinZhen", null,null,
                            "台北路一號",null));
        } catch (UserNotFoundException e) {
            log.error(e.getMsg());
        }
    }//查無使用者

    @Test
    void setUserToHotelerFromUserId() {
        userService.setUserToHotelerFromUserId(3L);
    }

}
