package com.yuu.community.service.Impl;

import com.yuu.community.service.LikeService;
import com.yuu.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {
    @Autowired
    private RedisTemplate redisTemplate;
    //点赞或取消赞
    @Override
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey=RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                //被点赞的用户
                String userLikeKey=RedisKeyUtil.getUserLikeKey(entityUserId);
                //判断redis中是否已经有存储该赞
                //查询要放在队列之外才会查询是有用的
                boolean isMember=operations.opsForSet().isMember(entityLikeKey,userId);
                // 开启执行队列
                operations.multi();
                if(isMember){
                    //表示已经点过赞了，现在要取消赞
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                }else{
                    //没点过赞，现在点赞
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);

                }
                return operations.exec();
            }
        });



    }
    //查询某个实体点赞的数量
    @Override
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey=RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }
    //查询某人对某个实体点赞的状态
    //1表示有点过赞
    //0表示没点过赞
    @Override
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey=RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId)? 1 : 0;
    }
    //查询某个用户获得的赞
    @Override
    public int findUserLikeCount(int userId) {
        String userLikeKey=RedisKeyUtil.getUserLikeKey(userId);
        Integer count= (Integer) redisTemplate.opsForValue().get(userLikeKey);  //获得key对应的value

        return count==null ? 0 : count.intValue();
    }


}
