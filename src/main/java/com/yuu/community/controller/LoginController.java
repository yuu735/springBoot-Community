package com.yuu.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.code.kaptcha.Producer;
import com.yuu.community.dao.LoginTicketDao;
import com.yuu.community.entity.User;
import com.yuu.community.service.UserService;
import com.yuu.community.util.CommunityUtil;
import com.yuu.community.util.Constant;
import com.yuu.community.util.MailClient;
import com.yuu.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


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
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private MailClient mailClient;
    //前往登录页面
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "site/login";
    }

    @RequestMapping(path="/login",method=RequestMethod.POST)
    public String login(String username,String password,String code,boolean rememberMe,
                        Model model,HttpServletResponse response,@CookieValue("kaptchaOwner")String kaptchaOwner){
        //从redis中获取验证码(需要有key(kaptchaOwner)才能找到当前登录者对应的验证码是哪个,而浏览器中有一个cookie存储了这个值)
        String kaptcha=null;
        if(StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey=RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha=(String)redisTemplate.opsForValue().get(redisKey);
        }

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
            cookie.setPath(contextPath);    //  cookie生效范围： /community
            cookie.setMaxAge(expiredSeconds);   //过期时间
            response.addCookie(cookie); //响应给浏览器！！

            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "site/login";
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        //找到名为ticket的cookie保存的value值，赋值给ticket字符串！
        userService.logout(ticket);
        //清理securityContext中的认证
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    @RequestMapping(path="/kaptcha",method=RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response){
        //  生成验证码
        String text=producer.createText();
        BufferedImage image=producer.createImage(text);

        //验证码的归属者
        String kaptchaOwner= CommunityUtil.generateUUID();
        //建立一个cookie记录：验证码的归属者，以便登录时可以从cookie取值进行判断
        Cookie cookie=new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);//验证码存活时间60秒
        cookie.setPath(contextPath);    //整个路径有效
        response.addCookie(cookie);     //存到浏览器
        //将验证码存入redis
        String redisKey= RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS); //key,value,生存时间，时间单位
        //之后会在登录的时候调用redis数据进行验证！

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

    //前往忘记密码页面
    @RequestMapping(path = "/forget",method = RequestMethod.GET)
    public String  getForgetPage() {
        return "site/forget";
    }

    //发送忘记密码的验证码
    @RequestMapping(path = "/sendVerdify",method = RequestMethod.POST)
    @ResponseBody
    public String sendVerdify(String email,HttpServletResponse response) {
        System.out.println(email);
        User u=userService.findUserByEmail(email);
        //封装成json对象
        JSONObject json=new JSONObject();
        if(u==null){
            json.put("emailMsg","该邮箱未被注册！请注册用户");
            return json.toJSONString();
        }
        //生成验证码
        String text=producer.createText();
        System.out.println("当前验证忘记密码的验证码为="+text);
        //验证码的归属者
        String pwdKaptchaOwner= CommunityUtil.generateUUID();
        //建立一个cookie记录：验证码的归属者，以便登录时可以从cookie取值进行判断
        Cookie cookie=new Cookie("pwdKaptchaOwner",pwdKaptchaOwner);
        cookie.setMaxAge(60*5);//验证码存活时间5分
        cookie.setPath(contextPath);    //整个路径有效
        response.addCookie(cookie);     //存到浏览器
        //将验证码存入redis
        String redisKey= RedisKeyUtil.getpwdKaptchaKey(pwdKaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60*5, TimeUnit.SECONDS); //key,value,生存时间，时间单位
        //之后会在登录的时候调用redis数据进行验证！

//        //发送邮件
//        Context context= new Context();
//        context.setVariable("email", u.getEmail());
//        context.setVariable("verdify",text);    //验证码
//        String content= templateEngine.process("/mail/forget",context);
//        mailClient.senMail(u.getEmail(), "忘记密码", content);
        userService.sendVerdifyMail(text,u);
        json.put("sendMsg","验证码发送成功请查看邮箱!");
        return json.toJSONString();
    }
    //重置密码
    @RequestMapping(path = "/newPassword",method = RequestMethod.POST)
    public String newPassword(String email,String verify,String password,Model model
            ,@CookieValue("pwdKaptchaOwner")String pwdKaptchaOwner){
        //获取要修改密码的user
        User u=userService.findUserByEmail(email);
        if(u==null){
            model.addAttribute("emailMsg","该邮箱未被注册！请注册用户");
            return "site/forget";
        }
        //开始检查验证码
        String kaptcha=null;
        if(StringUtils.isNotBlank(pwdKaptchaOwner)){
            String redisKey=RedisKeyUtil.getpwdKaptchaKey(pwdKaptchaOwner);
            kaptcha=(String)redisTemplate.opsForValue().get(redisKey);
        }
        if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(verify)|| !kaptcha.equalsIgnoreCase(verify)){
            model.addAttribute("codeMsg","验证码不正确");
            return "site/forget";
        }
        //检查有无输入新密码
        if(StringUtils.isBlank(password)){
            model.addAttribute("pwdMsg","新密码不能为空");
            return "site/forget";
        }
        //修改账号密码
        String newPassword=CommunityUtil.md5(password+u.getSalt());
        userService.updatePassword(u.getId(),newPassword);
        return "redirect:/login";

    }

}
