package com.yuu.community.interceptor;

import com.yuu.community.entity.LoginTicket;
import com.yuu.community.entity.User;
import com.yuu.community.service.UserService;
import com.yuu.community.util.CookieUtil;
import com.yuu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 使用security后进行了一下修改，preHandle和afterrHandle都有
 * preHandle：建构用户认证的结果，并存入securityContext中，以便于security进行授权
 * 我们在每次请求开始都做了判断，那么请求结束后也需要做一个清理，因此securityContext需要被清理！在Logincontroller那边登出请求时进行清理
 */
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
            //查询凭证
            LoginTicket loginTicket=userService.findLoginTicket(ticket);  //找到指定的ticket然后封装成对象
            //状态没有过期并且过期时间比当前时间晚才算是登录成功
            if(loginTicket!=null && loginTicket.getStatus()==0 && loginTicket.getExpired().after(new Date())){
                //根据凭证查询登录的用户
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求持有用户
                hostHolder.setUser(user);
                //建构用户认证的结果，并存入securityContext中，以便于security进行授权
                Authentication authentication=new UsernamePasswordAuthenticationToken(
                        user,user.getPassword(),userService.getAuthorities(user.getId())
                );
                //认证存到securityContext中
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }
    //在controller之后 模版之前调用的该方法！！将用户信息放入到model中
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user= hostHolder.getUser();
       if(user!=null && modelAndView!=null){
           modelAndView.addObject("loginUser",user);
       }
    }

    //在模版都执行完后将数据清理掉免得占用空间

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();

    }
}
