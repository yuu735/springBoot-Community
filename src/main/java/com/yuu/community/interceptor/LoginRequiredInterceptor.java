package com.yuu.community.interceptor;

import com.yuu.community.annotation.LoginRequired;
import com.yuu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 判断有无登录是否可执行方法
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断拦截的目标是不是一个方法，例如可能拦截的是静态资源因此要判断(这里静态资源在config中过滤掉了)
        if(handler instanceof HandlerMethod){
            //如果是方法会是HandlerMethod类型
            HandlerMethod handlerMethod=(HandlerMethod) handler;
            //获取拦截的方法对象
            Method method=handlerMethod.getMethod();
            //获取方法对象的注解
            LoginRequired loginRequired=method.getAnnotation(LoginRequired.class);
            //不为空表示需要登录后才可以访问的！！！
            //因此还需要判断有无登录
            if(loginRequired!=null && hostHolder.getUser()==null){
                //重定向去登录
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
        }
        return true;
    }
}
