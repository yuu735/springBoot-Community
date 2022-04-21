package com.yuu.community;

import com.yuu.community.entity.User;
import com.yuu.community.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CommunityApplicationTests {
    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
        User user=userService.findUserById(1);
        System.out.println(user);
    }

}
