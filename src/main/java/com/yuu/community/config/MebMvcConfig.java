package com.yuu.community.config;

import com.yuu.community.interceptor.LoginRequiredInterceptor;
import com.yuu.community.interceptor.LoginTicketInteceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MebMvcConfig implements WebMvcConfigurer{
    @Autowired
    private LoginTicketInteceptor loginTicketInteceptor;
    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInteceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
                //excludePathPatterns：排除静态资源，不需要拦截
                //所有页面都需要拦截所以不需要写addPathPatterns
        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
