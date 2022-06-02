package com.yuu.community.config;
import com.yuu.community.util.CommunityUtil;
import com.yuu.community.util.Constant;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    //忽略静态资源不用拦截
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    //授权
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //哪些需要登录后才能访问
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/user/updatePassword",
                        "/user/mypost/**",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                //什么权限可以访问
                .hasAnyAuthority(
                        Constant.AUTHORITY_USER,
                        Constant.AUTHORITY_ADMIN,
                        Constant.AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                )
                .hasAnyAuthority(
                        Constant.AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/delete",
                        "/data/**"

                )
                .hasAnyAuthority(
                        Constant.AUTHORITY_ADMIN
                )
                //其他任何的请求都允许访问
                .anyRequest().permitAll()
                        .and().csrf().disable();    //禁用csrf攻击的检查
        //权限不够时的处理
        http.exceptionHandling()
                //没有登录的处理
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        //如果是同步的普通请求还是异步请求（json）会有不同的处理结果
                        String xRequestPath= request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestPath)){
                            //表示为异步请求
                            //设定返回的格式是普通字符串
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer=response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你还没有登录哦"));
                        }else{
                            //表示为同步请求
                            // //localhost:8080/community/login
                            response.sendRedirect(request.getContextPath()+"/login");
                        }

                    }
                })
                //没有权限的处理
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        //如果是同步的普通请求还是异步请求（json）会有不同的处理结果
                        String xRequestPath= request.getHeader("x-requested-with");
                        if(xRequestPath.equals("XMLHttpRequest")){
                            //表示为异步请求
                            //设定返回的格式是普通字符串
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer=response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你没有访问此功能的权限！"));
                        }else{
                            //表示为同步请求
                            // //localhost:8080/community/login
                            response.sendRedirect(request.getContextPath()+"/denied");
                        }
                    }
                });
        //security的退出是在controller之前就会进行拦截
        //security底层默认会拦截/logout请求，进行退出处理！
        //覆盖它默认的逻辑，才能执行我们自己的退出代码
        http.logout().logoutUrl("/securitylogout");//修改security拦截的登出路径是哪个

    }
}
