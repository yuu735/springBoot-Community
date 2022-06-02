package com.yuu.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 自定义的redisTemplate
 */
@Configuration
public class RedisConfig {
    //我们自定义的redisTemplate 为了让它具备访问数据库的能力，需要把数据库注入进来(RedisConnectionFactory factory)
    // 这样就能访问数据库了
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String,Object> template=new RedisTemplate<>();
        template.setConnectionFactory(factory);
        //设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        //value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        //hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        template.afterPropertiesSet();
        return template;
    }
}
