package com.yuu.community.controller;

import com.google.code.kaptcha.Producer;
import com.yuu.community.dao.LoginTicketDao;
import com.yuu.community.entity.User;
import com.yuu.community.service.UserService;
import com.yuu.community.util.Constant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static com.yuu.community.util.Constant.*;

@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private LoginTicketDao loginTicketDao;
    @Autowired
    private Producer producer;
    @Autowired
    private UserService userService;
    //前往登录页面
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "site/login";
    }
    @RequestMapping(path="/login",method=RequestMethod.POST)
    public String login(String username,String password,String code,boolean rememberMe,Model model,HttpSession session,HttpServletResponse response){
        String kaptcha=(String)session.getAttribute("kaptcha");
        if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)|| !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确");
            return "site/login";
        }
        //检查账号密码
        int expiredSeconds=rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map=userService.login(username,password,expiredSeconds);
        //表示登录成功
        if(map.containsKey("ticket")){
            Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);    //   /community
            cookie.setMaxAge(expiredSeconds);   //过期时间
            response.addCookie(cookie); //响应给浏览器

            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "site/login";
        }
    }
    @RequestMapping("/logout")
    public void logout(String ticket){
        loginTicketDao.updateStatus(ticket,1);
    }
    @RequestMapping(path="/kaptcha",method=RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //  生成验证码
        String text=producer.createText();
        BufferedImage image=producer.createImage(text);
        //将验证码存入session
        session.setAttribute("kaptcha",text);
        response.setContentType("image/png");
        try{
            OutputStream os=response.getOutputStream();
            ImageIO.write(image,"png",os);
        }catch (IOException e){
            logger.error("响应验证码失败"+e.getMessage());
        }
    }

    //前往注册页面，这里是get方法走跳转页面！！
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "site/register";
    }
    //注意！这里path和上面一样，但是根据收到的请求不同选择不同的方法执行！！！post方法是处理表单提交数据
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) { //表示没有任何错误信息=注册成功
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "site/register";
        }
    }

    //激活账户
    //自己决定激活链接要由什么路径所组成，这里选择根据用户id和激活码
    // http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
     public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        System.out.println("链接激活的结果=>"+result);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "site/operate-result";
    }
}
