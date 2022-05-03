package com.yuu.community.service.Impl;

import com.yuu.community.dao.LoginTicketDao;
import com.yuu.community.dao.UserDao;
import com.yuu.community.entity.LoginTicket;
import com.yuu.community.entity.User;
import com.yuu.community.service.UserService;
import com.yuu.community.util.CommunityUtil;
import com.yuu.community.util.Constant;
import com.yuu.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private LoginTicketDao loginTicketDao;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserDao userDao;

    @Override
    public Map<String, Object> register(User user) {
        Map<String,Object> map=new HashMap<>();
        if(user==null)
            throw new IllegalArgumentException("参数不能为空");
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
        }if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
        }if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
        }
        //认证user是否已经注册
        User u= userDao.selectByName(user.getUsername());
        if(u!=null){
            map.put("usernameMsg","该账号已存在");
            return map;
        }
        //认证email是否已经注册
        u= userDao.selectByEmail(user.getEmail());
        if(u!=null){
            map.put("emailMsg","该邮箱已经注册");
            return map;
        }
        //注册用户
        //对密码加密
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));//随机字符
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);//普通用户
        user.setStatus(0);//未激活
        user.setActivationCode(CommunityUtil.generateUUID());//激活码
        //初始头像
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        System.out.println("初始头像:"+user.getHeaderUrl());
        user.setCreateTime(new Date());
        userDao.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        System.out.println("激活链接=>"+url);
        mailClient.senMail(user.getEmail(), "激活账号", content);

        return map;
    }
    //验证激活链接！
    public int activation(int userId, String code) {
        User user = userDao.selectById(userId);
        if (user.getStatus() == 1) {
            return Constant.ACTIVATION_REPEAT;  //重复激活
        } else if (user.getActivationCode().equals(code)) {
            userDao.updateStatus(userId, 1);    //激活成功
            return Constant.ACTIVATION_SUCCESS;
        } else {
            return Constant.ACTIVATION_FAILURE;     //激活失败
        }
    }

    @Override
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String,Object> map=new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
        }if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
        }
        User user=userDao.selectByName(username);
        if(user==null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }
        if(user.getStatus()==0){
            map.put("usernameMsg","该账号未激活");
            return map;
        }
        //验证密码,先将输入的密码加密
        password=CommunityUtil.md5(password+user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","该密码不正确");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());        //登录用户id
        loginTicket.setTicket(CommunityUtil.generateUUID());//登录口令:随机字符串
        loginTicket.setStatus(0);   //登录状态0没过期，1过期
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));   //过期时间
        loginTicketDao.insertLoginTicket(loginTicket);  //添加登录凭证

        map.put("ticket", loginTicket.getTicket()); // ticket:登录口令（随机字符串）
        return map;
    }


    @Override
    public void logout(String ticket) {
        System.out.println("当前要退出的"+ticket);
        loginTicketDao.updateStatus(ticket, 1); //根据登录口令来判断是哪个login_ticket需要修改
    }

    @Override
    public int updateHeader(int id, String headerUrl) {
        return userDao.updateHeader(id,headerUrl);
    }


    @Override
    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketDao.selectByTicket(ticket);
    }

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
    public int updatePassword(int id, String password) {

        return userDao.updatePassword(id,password);
    }
}
