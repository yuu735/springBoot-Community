package com.yuu.community.service.Impl;

import com.yuu.community.entity.User;
import com.yuu.community.service.FollowService;
import com.yuu.community.service.UserService;
import com.yuu.community.util.Constant;
import com.yuu.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowServiceImpl implements FollowService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    //关注
    @Override
    public void follow(int userId, int entityType, int entityId) {
            redisTemplate.execute(new SessionCallback() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                    String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                    operations.multi();

                    operations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                    operations.opsForZSet().add(followerKey,userId,System.currentTimeMillis());

                    return operations.exec();
                }
            });
    }
    //取消关注
    @Override
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                operations.multi();

                operations.opsForZSet().remove(followeeKey,entityId);
                operations.opsForZSet().remove(followerKey,userId);

                return operations.exec();
            }
        });
    }
    //查询关注的实体数量
    @Override
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);

        return redisTemplate.opsForZSet().zCard(followeeKey);
    }
    //查询实体的粉丝数量
    @Override
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }
    //查询当前用户是否已经关注该实体
    @Override
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        //根据查询有无这个分数来判断有无这一组数据，如果有表示有关注，没有则表示没有关注
        return redisTemplate.opsForZSet().score(followeeKey,entityId)!=null;
    }
    //查询某用户关注的人
    @Override
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey=RedisKeyUtil.getFolloweeKey(userId, Constant.ENTITY_TYPE_USER);
        Set<Integer> targetIds=redisTemplate.opsForZSet().reverseRange(followeeKey,offset,offset+limit-1);
        if(targetIds==null){
            return  null;
        }
        List<Map<String, Object>> list=new ArrayList<>();
        for(Integer targetId:targetIds){
            Map<String, Object> map=new HashMap<>();
            User user=userService.findUserById(targetId);
            map.put("user",user);
            //关注时间
            Double score=redisTemplate.opsForZSet().score(followeeKey,targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }
    //查询某用户的粉丝
    @Override
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(Constant.ENTITY_TYPE_USER,userId);
        //倒叙
        Set<Integer> targetIds=redisTemplate.opsForZSet().reverseRange(followerKey,offset,offset+limit-1);
        if(targetIds==null){
            return  null;
        }
        List<Map<String, Object>> list=new ArrayList<>();
        for(Integer targetId:targetIds){
            Map<String, Object> map=new HashMap<>();
            User user=userService.findUserById(targetId);
            map.put("user",user);
            //关注时间
            Double score=redisTemplate.opsForZSet().score(followerKey,targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }
}
