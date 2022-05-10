package com.yuu.community.controller;

import com.yuu.community.annotation.LoginRequired;
import com.yuu.community.entity.Comment;
import com.yuu.community.entity.DiscussPost;
import com.yuu.community.entity.Event;
import com.yuu.community.event.EventProducer;
import com.yuu.community.service.CommentService;
import com.yuu.community.service.DiscussPostService;
import com.yuu.community.util.Constant;
import com.yuu.community.util.HostHolder;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private DiscussPostService discussPostService;
    @LoginRequired
    @RequestMapping(path="/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId")int discussPostId, Comment comment){
        //补充comment的创建时间、状态、发布者
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);//有效
        commentService.addComment(comment);
        //添加后触发评论事件
        Event event=new Event().setTopic(Constant.TOPIC_COMMENT).setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType()).setEntityId(comment.getEntityId())
                .setData("postId",discussPostId);
        //设置目标所属者的id
        //如果是给帖子（目标）做评论
        if(comment.getEntityType()==Constant.ENTITY_TYPE_POST){
            //评论的目标
            DiscussPost target =discussPostService.findDiscussPostById(comment.getEntityId());//   根据评论的实体id找到对应的帖子id
            event.setEntityUserId((target.getUserId()));
        }
        else if(comment.getEntityType()==Constant.ENTITY_TYPE_COMMENT){
            //或者是给评论做评论
            Comment target=commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);
        //评论帖子才触发更新帖子事件
        if(comment.getEntityType()== Constant.ENTITY_TYPE_POST){
            //触发发帖事件（存到elasticsearch）
            event=new Event()
                .setTopic(Constant.TOPIC_PUBLISH)
                .setUserId(comment.getUserId())
                .setEntityType(Constant.ENTITY_TYPE_POST)
                .setEntityId(discussPostId);

            eventProducer.fireEvent(event);
        }
        return "redirect:/discuss/detail/"+discussPostId;
    }
}
