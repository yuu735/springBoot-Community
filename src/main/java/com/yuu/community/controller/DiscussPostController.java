package com.yuu.community.controller;

import com.yuu.community.entity.*;
import com.yuu.community.event.EventProducer;
import com.yuu.community.service.CommentService;
import com.yuu.community.service.DiscussPostService;
import com.yuu.community.service.LikeService;
import com.yuu.community.service.UserService;
import com.yuu.community.util.CommunityUtil;
import com.yuu.community.util.Constant;
import com.yuu.community.util.HostHolder;
import com.yuu.community.util.RedisKeyUtil;
import org.apache.catalina.Host;
import org.elasticsearch.client.eql.EqlSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;


    @RequestMapping(path="/add",method= RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        //登录后才能发帖子
        User user=hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403,"你还没有登录哦！");
        }
        DiscussPost post=new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        //触发发帖事件（存到elasticsearch）
        Event event=new Event()
                .setTopic(Constant.TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(Constant.ENTITY_TYPE_POST)
                .setEntityId(post.getId());

        eventProducer.fireEvent(event);

        //计算帖子分数
        String redisKey= RedisKeyUtil.getPostScoreKey();
        //放到redis中
        redisTemplate.opsForSet().add(redisKey,post.getId());


        //报错的情况将来统一处理
        return CommunityUtil.getJSONString(0,"发布成功");
    }

    @RequestMapping(path="/detail/{discussPostId}",method= RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //帖子
        DiscussPost post=discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        //作者
        User user=userService.findUserById(post.getUserId());
        model.addAttribute("user",user);
        //帖子的赞数量
        long likeCount=likeService.findEntityLikeCount(Constant.ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeCount",likeCount);
        //点赞状态,如果没登录就返回0表示未点赞否则进行查询！！！
        int likeStatus=hostHolder.getUser()==null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(),Constant.ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeStatus",likeStatus);

        //评论分页
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(post.getCommentCount());//总评论数
        //评论有两种分为：
            //评论：给帖子的评论
            //回复：给评论的评论

        //先获取评论对象的列表（还要对数据进行删选要呈现给前端的再放入到model中）
        List<Comment> commentList=commentService.findCommentsByEntity(Constant.ENTITY_TYPE_POST,post.getId(),page.getOffset(),page.getLimit());
        //评论vo列表（真正放入model的列表）
        List<Map<String,Object>> commentVoList=new ArrayList<>();//对展现的数据统一的封装(viewObject)显示的对象
        if(commentList!=null){
            for(Comment comment:commentList){
                //封装评论vo：用来封装呈现给页面的数据
                Map<String,Object> commentVo=new HashMap<>();
                //评论
                commentVo.put("comment",comment);
                //当前评论的发布者是谁
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                //评论的赞数量
                likeCount=likeService.findEntityLikeCount(Constant.ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",likeCount);
                //评论的点赞状态,如果没登录就返回0表示未点赞否则进行查询！！！
                likeStatus=hostHolder.getUser()==null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(),Constant.ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeStatus",likeStatus);

                //该评论的回复列表-所以要用的是comment的id
                List<Comment> replyList= commentService.findCommentsByEntity(Constant.ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);
                //回复的vo列表
                List<Map<String,Object>> replyVoList=new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 回复目标（楼中楼的情况：a回复b说：xxx。b就是target）
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        //评论的赞数量
                        likeCount=likeService.findEntityLikeCount(Constant.ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeCount",likeCount);
                        //评论的点赞状态,如果没登录就返回0表示未点赞否则进行查询！！！
                        likeStatus=hostHolder.getUser()==null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(),Constant.ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeStatus",likeStatus);


                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);
                //回复数量：当前评论有几个回复
                int replyCount= commentService.findCommentCount(Constant.ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("replyCount",replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);
        return "site/discuss-detail";
    }

    //置顶/取消置顶
    @RequestMapping(path="/top",method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        DiscussPost post=discussPostService.findDiscussPostById(id);
        //获取置顶状态：1-置顶，0-没置顶。此时如果getType=1表示目前想要执行取消置顶操作,getType=0表示目前想要执行置顶操作
        // 1^1=-,0^1=1
        int type=post.getType()^1;
        discussPostService.updateType(id,type);
        //返回的结构
        Map<String,Object> map=new HashMap<>();
        map.put("type",type);

        //触发发帖事件（更改帖子状态）
        Event event=new Event()
                .setTopic(Constant.TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(Constant.ENTITY_TYPE_POST)
                .setEntityId(id);

        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0,null,map);
    }
    //加精/取消加精
    @RequestMapping(path="/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        DiscussPost post=discussPostService.findDiscussPostById(id);
        //但这里不考虑拉黑状态了
        //获取加精状态：0-正常，1-加精，2-拉黑删除。
        System.out.println("当前帖子的状态是="+post.getStatus());
        int status=post.getStatus()^1;
        System.out.println("需要修改状态为="+post.getStatus());
        discussPostService.updateStatus(id,status);
        //返回的结果
        Map<String,Object> map=new HashMap<>();
        map.put("status",status);
        System.out.println("当前修改后的状态是"+post.getStatus());

        //触发发帖事件（更改帖子状态）
        Event event=new Event()
                .setTopic(Constant.TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(Constant.ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        //计算帖子分数
        String redisKey= RedisKeyUtil.getPostScoreKey();
        //放到redis中
        redisTemplate.opsForSet().add(redisKey,post.getId());

        return CommunityUtil.getJSONString(0,null,map);
    }
    //删除(拉黑)
    @RequestMapping(path="/delete",method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        discussPostService.updateStatus(id,2);
        commentService.updateStatus(getIds(id),2);
        //触发删除事件
        Event event=new Event()
                .setTopic(Constant.TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(Constant.ENTITY_TYPE_POST)
                .setEntityId(id);

        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0,null);
    }

    //获取删除帖子所对应的所有comment集合（存放的是comment的id）
    private List<Integer> getIds(int entityId) {
        //先找出entity_type=1和要删除帖子的entity_id
        List<Integer> ids = new ArrayList<>();
        List<Comment> type1List=commentService.selectCommentsByTypeAndId(Constant.ENTITY_TYPE_POST,entityId);
        if(type1List!=null){
            for(Comment comment:type1List)
                ids.add(comment.getId());
        }
        //再找出ids中的id此时作为entity_id，entity_type=2的表示回复中的回复，然后记住该评论id也是需要修改状态的！
        //如果在遍历集合的过程中要修改集合，必须用get()取值才不会出错
        if(ids.size()>0){
            for(int i=0;i<ids.size();i++){
                List<Comment> type2List=commentService.selectCommentsByTypeAndId(Constant.ENTITY_TYPE_COMMENT,ids.get(i));
                if(type2List!=null){
                    for(Comment c:type2List){
                        ids.add(c.getId());
                    }
                }
            }
        }
        return ids;
    }
}
