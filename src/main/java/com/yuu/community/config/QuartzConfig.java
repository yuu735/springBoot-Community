package com.yuu.community.config;

import com.yuu.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

//配置->数据库->调用
@Configuration
public class QuartzConfig {
    // FactoryBean可简化Bean的实例化过程:
    // 1.通过FactoryBean封装Bean的实例化过程.
    // 2.将FactoryBean装配到Spring容器里.
    // 3.将FactoryBean注入给其他的Bean.
    // 4.该Bean得到的是FactoryBean所管理的对象实例.

    //刷新帖子分数的任务
    @Bean
    public JobDetailFactoryBean PostScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean=new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("PostScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }
    @Bean
    public SimpleTriggerFactoryBean PostScoreRefreshTrigger(JobDetail PostScoreRefreshJobDetail){
        SimpleTriggerFactoryBean factoryBean=new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(PostScoreRefreshJobDetail);
        factoryBean.setName("PostScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000*60*5);//五分钟计算一次
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}
