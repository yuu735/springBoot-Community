package com.yuu.community.controller;

import com.yuu.community.annotation.LoginRequired;
import com.yuu.community.entity.User;
import com.yuu.community.service.LikeService;
import com.yuu.community.service.UserService;
import com.yuu.community.util.CommunityUtil;
import com.yuu.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;

    @LoginRequired
    @RequestMapping(path="/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "site/setting";
    }

    @LoginRequired
    @RequestMapping(path="/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "site/setting";
        }
        //获取文件的后缀
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确!");
            return "site/setting";
        }
        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() +"."+suffix;
        System.out.println("随机文件名=>"+fileName);
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        // 更新当前用户的头像的路径(web访问路径)
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        System.out.println("web图片访问路径=>"+headerUrl);
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    //获取头像，没登录也可以查看别人的头像，因此这个不需要
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }
    //修改密码
    @LoginRequired
    @RequestMapping(path = "/updatePassword",method = RequestMethod.POST)
    public String updatePassword(String oldPassword,String newPassword,String confirmPassword,Model model,@CookieValue("ticket") String ticket){
        if(StringUtils.isBlank(oldPassword)||StringUtils.isBlank(newPassword)|| StringUtils.isBlank(confirmPassword)){
            model.addAttribute("oldPasswordMsg","密码不能为空");
            return "site/setting";
        }
        User user=hostHolder.getUser();
        String pw=user.getPassword();
        //将输入的密码进行加密才能和数据库中已经加密的密码进行比较
        oldPassword=CommunityUtil.md5(oldPassword+user.getSalt());
        if(!pw.equals(oldPassword)){
           model.addAttribute("oldPasswordMsg","输入的密码不正确");
            return "site/setting";
        }
        if(!newPassword.equals(confirmPassword)){
            model.addAttribute("newPasswordMsg","两次输入的密码不相同");
            model.addAttribute("confirmPasswordMsg","两次输入的密码不相同");
            return "site/setting";
        }
        //修改密码
        confirmPassword=CommunityUtil.md5(confirmPassword+user.getSalt());  //新密码加密
        userService.updatePassword(user.getId(),confirmPassword);   //修改密码
        userService.logout(ticket); //退出重新登录
        return "redirect:/login";
    }

    //个人主页(也可以查看别人的主页)
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfile(@PathVariable("userId")int userId,Model model){
        User user=userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        //用户基本信息
        model.addAttribute("user",user);
        //点赞数量
        int likeCount=likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        return "site/profile";
    }
}
