package com.yuu.community.interceptor;

import com.yuu.community.entity.LoginTicket;
import com.yuu.community.entity.User;
import com.yuu.community.service.UserService;
import com.yuu.community.util.CookieUtil;
import com.yuu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInteceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //要获取cookie，从request中获取cookie，先对cookie进行封装
        //从cookies中获取名叫ticket的cookie里面存取的值（登录凭证）
        //System.out.print("执行preHandel=================");
        String ticket= CookieUtil.getValue(request,"ticket");
        if(ticket!=null){
            LoginTicket loginTicket=userService.findLoginTicket(ticket);  //找到指定的ticket然后封装成对象
            //状态没有过期并且过期时间比当前时间晚才算是登录成功
            if(loginTicket!=null && loginTicket.getStatus()==0 && loginTicket.getExpired().after(new Date())){
                //根据凭证查询登录的用户
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求持有用户
                hostHolder.setUser(user);
               // System.out.println("持有用户成功:"+hostHolder.getUser());
            }
        }
        return true;
    }
    //在controller之后 模版之前调用的该方法！！将用户信息放入到model中
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
       // System.out.println("执行postHandel=================");
        User user= hostHolder.getUser();
       if(user!=null && modelAndView!=null){
           modelAndView.addObject("loginUser",user);
       }
    }
    //在模版都执行完后将数据清理掉免得占用空间
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
       // System.out.print("执行afterCompletion清除hostHolder=================");
        hostHolder.clear();

    }
}
