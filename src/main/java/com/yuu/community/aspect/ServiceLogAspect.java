package com.yuu.community.aspect;

import org.aopalliance.intercept.Joinpoint;
import org.apache.coyote.Request;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 统一记录日志，使用AOP来实现
 */
@Component
@Aspect
public class ServiceLogAspect {
    private static final Logger logger= LoggerFactory.getLogger(ServiceLogAspect.class);
    //service包下的所有文件中的所有方法
    @Pointcut("execution(* com.yuu.community.service.*.*(..))")
    public void pointcut(){
    }

    //通知
    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        //格式：用户[1.2.3.4],在[xxx时间],访问了[com.....service...()方法]
        ServletRequestAttributes attributes=(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //一个请求到达容器后，Spring会把该请求Request实例通过setRequestAttributes方法 把Request实例放入该请求线程内ThreadLocalMap中，然后就可以通过静态方法取到
        if(attributes==null){
           return;
        }
        //attributes有可能为空，并不是所有都在controller调用。像消费者和生产者就不是在controller调用!
        HttpServletRequest request = attributes.getRequest();
        String ip=request.getRemoteHost();
        String now=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target= joinPoint.getSignature().getDeclaringType()+"."+joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s],在[%s],访问了[%s].",ip,now,target));
    }
}
