package com.yuu.community.service.Impl;

import com.yuu.community.dao.UserDao;
import com.yuu.community.entity.User;
import com.yuu.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Override
    public User findUserById(int id) {
        return userDao.selectById(id);
    }

    @Override
    public User findUserByName(String username) {
        return userDao.selectByName(username);
    }

    @Override
    public User findUserByEmail(String email) {
        return userDao.selectByEmail(email);
    }

    @Override
    public int addUser(User user) {
        return userDao.insertUser(user);
    }

    @Override
    public int updateStatus(int id, int status) {
        return userDao.updateStatus(id,status);
    }

    @Override
    public int updateHeader(int id, String headerUrl) {
        return userDao.updateHeader(id,headerUrl);
    }

    @Override
    public int updatePassword(int id, String password) {
        return userDao.updatePassword(id,password);
    }
}
