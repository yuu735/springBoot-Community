package com.yuu.community.quartz;

import com.yuu.community.entity.DiscussPost;
import com.yuu.community.service.CommentService;
import com.yuu.community.service.DiscussPostService;
import com.yuu.community.service.Impl.ElasticsearchService;
import com.yuu.community.service.LikeService;
import com.yuu.community.util.Constant;
import com.yuu.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * quartz计算帖子分数
 */
public class PostScoreRefreshJob implements Job {
    private static final Logger logger= LoggerFactory.getLogger(PostScoreRefreshJob.class);
    @Autowired
    private RedisTemplate redisTemplate;

    private static final Date epoch;
    static {
        try {
            epoch=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败!",e);
        }
    }
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey= RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations=redisTemplate.boundSetOps(redisKey);
        if(operations.size()==0) {
            logger.info("[任务取消]没有需要刷新的帖子!");
            return;
        }
        //刷新
        logger.info("[任务开始]正在刷新帖子分数："+operations.size());
        while(operations.size()>0){
            this.refresh((Integer)operations.pop());
        }
        logger.info("[任务结束]帖子分数刷新完毕！");


    }
    private void refresh(int postId){
        DiscussPost post=discussPostService.findDiscussPostById(postId);
        if(post==null){
            logger.error("该帖子不存在：id="+postId);
        }
        //开始算分

        //是否加精
        boolean wonderful=post.getStatus()==1;
        //评论数量
        int commentCount=post.getCommentCount();
        //评论数量
        long likeCount=likeService.findEntityLikeCount(Constant.ENTITY_TYPE_POST,postId);

        //计算权重
        double w=(wonderful ? 75:0)+commentCount*10+likeCount*2;
        //分数=帖子权重+距离天数, 避免w=0所以用max
        double score=Math.log10(Math.max(w,1))+(post.getCreateTime().getTime()-epoch.getTime())/(1000*3600*24);
        //更新帖子分数
        discussPostService.updateScore(post.getId(),score);
        post.setScore(score);
        //同步搜寻数据es
        elasticsearchService.saveDiscussPost(post);

    }
}
