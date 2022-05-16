package com.yuu.community.controller;

import com.yuu.community.annotation.LoginRequired;
import com.yuu.community.entity.Event;
import com.yuu.community.entity.User;
import com.yuu.community.event.EventProducer;
import com.yuu.community.service.LikeService;
import com.yuu.community.util.CommunityUtil;
import com.yuu.community.util.Constant;
import com.yuu.community.util.HostHolder;
import com.yuu.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;

    @LoginRequired
    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId,int postId){
        System.out.println("执行点赞功能======================");
        User user=hostHolder.getUser();
        //实现点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        long likeCount=likeService.findEntityLikeCount(entityType,entityId);
        //状态
        int likeStatus=likeService.findEntityLikeStatus(user.getId(),entityType,entityId);
        Map<String,Object> map=new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);
        //触发点赞事件,点赞才触发！
        if(likeStatus==1){
            Event event=new Event().setTopic(Constant.TOPIC_LIKE).setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType).setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId);
            eventProducer.fireEvent(event);

        }
        //对帖子点赞才算
        if(entityType==Constant.ENTITY_TYPE_POST){
            //计算帖子分数
            String redisKey= RedisKeyUtil.getPostScoreKey();
            //放到redis中
            redisTemplate.opsForSet().add(redisKey,postId);
        }
        return CommunityUtil.getJSONString(0,null,map);
    }
}
