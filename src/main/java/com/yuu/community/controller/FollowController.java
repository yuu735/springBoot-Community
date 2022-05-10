package com.yuu.community.controller;

import com.yuu.community.annotation.LoginRequired;
import com.yuu.community.entity.Event;
import com.yuu.community.entity.Page;
import com.yuu.community.entity.User;
import com.yuu.community.event.EventProducer;
import com.yuu.community.service.FollowService;
import com.yuu.community.service.UserService;
import com.yuu.community.util.CommunityUtil;
import com.yuu.community.util.Constant;
import com.yuu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController {
    @Autowired
    private FollowService followService;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;
    @LoginRequired
    @RequestMapping(path="/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user=hostHolder.getUser();

        followService.follow(user.getId(), entityType,entityId);
        //触发关注事件
        Event event=new Event()
                .setTopic(Constant.TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"已关注");
    }
    @RequestMapping(path="/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user=hostHolder.getUser();

        followService.unfollow(user.getId(), entityType,entityId);
        return CommunityUtil.getJSONString(0,"取消关注");
    }
    //显示关注的列表
    @RequestMapping(path="/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        User user=userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followees/"+userId);
        page.setRows((int)followService.findFolloweeCount(userId, Constant.ENTITY_TYPE_USER));
        List<Map<String,Object>> userList=followService.findFollowees(userId,page.getOffset(),page.getLimit());
        if(userList!=null){
            //遍历元素
            for(Map<String,Object> map:userList){
                User u=(User)map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));  //我是否有关注这个用户
            }
        }
        model.addAttribute("users",userList);
        return "site/followee";
    }
    //当前用户对他(查看的对象)的关注状态
    private boolean hasFollowed(int userId){
        if(hostHolder.getUser()==null){
            //没登录就不可能关注过他
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(),Constant.ENTITY_TYPE_USER,userId);
    }

    //显示粉丝的列表
    @RequestMapping(path="/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User user=userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followers/"+userId);
        page.setRows((int)followService.findFollowerCount(Constant.ENTITY_TYPE_USER,userId));
        List<Map<String,Object>> userList=followService.findFollowers(userId,page.getOffset(),page.getLimit());
        if(userList!=null){
            //遍历元素 map目前里面有:user、followTime
            for(Map<String,Object> map:userList){
                User u=(User)map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));  //我是否有关注这个用户
            }
            //map目前里面有:user、followTime、hasFollowed
        }
        model.addAttribute("users",userList);
        return "site/follower";
    }
}
