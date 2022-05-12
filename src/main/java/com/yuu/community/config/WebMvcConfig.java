package com.yuu.community.config;

import com.yuu.community.interceptor.LoginRequiredInterceptor;
import com.yuu.community.interceptor.LoginTicketInteceptor;
import com.yuu.community.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 如果有多个拦截器被注册，会根据注册的顺序执行拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer{
    @Autowired
    private LoginTicketInteceptor loginTicketInteceptor;
   // @Autowired
   // private LoginRequiredInterceptor loginRequiredInterceptor;
    @Autowired
    private MessageInterceptor messageInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInteceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
                //excludePathPatterns：排除静态资源，不需要拦截
                //所有页面都需要拦截所以不需要写addPathPatterns

//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
