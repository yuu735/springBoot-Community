package com.yuu.community.util;

import com.yuu.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，代替session对象
 * 为了后续方便使用登录用户信息，需要暂存登录用户信息
 * 为了保证多线程并发（一个服务器处理多个浏览器请求）服务器在处理请求是多线程环境，因此为了让多线程并发时访问数据都没有问题，就需要考虑线程隔离
 * ThreadLocal实现线程隔离这个问题
 */
@Component
public class HostHolder{
    //初始化
    private ThreadLocal<User> users=new ThreadLocal<User>();
    public void setUser(User user){
        users.set(user);
    }
    public User getUser(){
        return users.get();
    }
    public void clear(){
        users.remove();
        //System.out.println("user删除！");
    }
}
