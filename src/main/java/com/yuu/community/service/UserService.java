package com.yuu.community.service;

import com.yuu.community.entity.User;

public interface UserService {
    public User findUserById(int id);
    public User findUserByName(String username);
    public User findUserByEmail(String email);
    int addUser(User user);
    int updateStatus(int id,int status);
    int updateHeader(int id,String headerUrl);
    int updatePassword(int id,String password);
}
