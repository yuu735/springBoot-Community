package com.yuu.community.service;

import com.yuu.community.entity.LoginTicket;
import com.yuu.community.entity.User;

import java.util.Map;

public interface UserService {
    Map<String,Object> register(User user);   //注册账户
    int activation(int userId, String code); //激活账户

    Map<String,Object> login(String username,String password,int expiredSeconds);
    LoginTicket findLoginTicket(String ticket);

    User findUserById(int id);
    User findUserByName(String username);
    User findUserByEmail(String email);
    int addUser(User user);
    int updateStatus(int id,int status);
    int updateHeader(int id,String headerUrl);
    int updatePassword(int id,String password);
}
