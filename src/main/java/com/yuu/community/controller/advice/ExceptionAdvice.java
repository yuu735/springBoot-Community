package com.yuu.community.controller.advice;

import com.yuu.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 在表现层统一处理异常
 */
//annotation限制范围，只去扫描带有controller注解的那些bean
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    private static final Logger logger= LoggerFactory.getLogger(ExceptionAdvice.class);
    //处理哪些异常呢？这里会处理所有异常因此使用异常的父类
    @ExceptionHandler({Exception.class})
    public void handlerException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        //记录异常
        logger.error("服务器发生异常: "+e.getMessage());
        for(StackTraceElement element : e.getStackTrace()){
            logger.error(element.toString());
        }
        //判断请求是 普通的请求（返回页面）还是异步请求（返回的是json）
        String xPrequesyWith=request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(xPrequesyWith)){
            //XMLHttpRequest这是一个异步请求返回的是xml
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter pw=response.getWriter();
            pw.write(CommunityUtil.getJSONString(1,"服务器异常"));
        }else{
            //这是一个普通请求
            response.sendRedirect(request.getContextPath()+"/error");
        }
    }

}
