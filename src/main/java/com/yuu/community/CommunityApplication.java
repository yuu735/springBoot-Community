package com.yuu.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {
    @PostConstruct  //管理bean的生命周期
    public void init(){
        //解决netty启动冲突的问题(elasticsearch和redis底层都是用netty，现在导入elast会和redis起冲突)
        System.setProperty("es.set.netty.runtime.available.processors","false");
    }

    public static void main(String[] args) {
        System.setProperty("es.set.netty.runtime.available.processors","false");
        SpringApplication.run(CommunityApplication.class, args);
    }

}
