package com.yuu.community.service.Impl;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.yuu.community.dao.DiscussDao;
import com.yuu.community.entity.DiscussPost;
import com.yuu.community.service.DiscussPostService;
import com.yuu.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostServiceImpl implements DiscussPostService {
    private static final Logger logger= LoggerFactory.getLogger(DiscussPostServiceImpl.class);
    @Value("${caffeine.posts.max-size}")
    private int maxSize;
    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;
    @Autowired
    private DiscussDao discussDao;
    @Autowired
    private SensitiveFilter sensitiveFilter;


    //Caffeinie核心接口:Cache,LoadingCache,AsyncLoadingCache
    //缓存帖子列表
    private LoadingCache<String,List<DiscussPost>> postListCache;

    //缓存总行数
    private LoadingCache<Integer,Integer> postRowsCache;
    @PostConstruct
    public void init(){
        //初始化帖子列表缓存
        postListCache= Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
                        //怎么从数据库查询数据
                        if(key==null || key.length()==0){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        String[] params=key.split(":");
                        if(params==null || params.length!=2){
                            throw new IllegalArgumentException("参数错误");
                        }
                        int offset=Integer.valueOf(params[0]);
                        int limit=Integer.valueOf(params[1]);
                        logger.debug("load post list from DB");
                        return discussDao.selectDiscussPosts(0,offset,limit,1);
                    }
                });
        //初始化帖子总数缓存
        postRowsCache= Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("load post list from DB");
                        return discussDao.selectDiscussPostRows(key);
                    }
                });
    }

    @Override
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit,int orderMode) {
        if(userId==0 && orderMode==1){
            // 启用缓存
            return postListCache.get(offset+":"+limit);
        }
        logger.debug("load post list from DB");
        return discussDao.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    @Override
    public int findDiscussPostRows(int userId) {
        if(userId==0){
            //首页查询,userId作为key
            return postRowsCache.get(userId);
        }
        logger.debug("load post rows from DB");
        return discussDao.selectDiscussPostRows(userId);
    }

    @Override
    public int addDiscussPost(DiscussPost post) {
        if(post==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //过滤数据（只有title和content需要进行敏感词过滤）
        //先转义html标签，将有html标签的变成普通字符串
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        //实现插入数据
        return discussDao.insertDiscussPost(post);

    }

    @Override
    public DiscussPost findDiscussPostById(int id) {

        return discussDao.selectDiscussPostById(id);
    }

    @Override
    public int updateCommentCount(int id, int commentCount) {
        return discussDao.updateCommentCount(id, commentCount);
    }

    @Override
    public int updateType(int id, int type) {
        return discussDao.updateType(id, type);
    }

    @Override
    public int updateStatus(int id, int status) {
        return discussDao.updateStatus(id,status);
    }

    @Override
    public int updateScore(int id, double score) {
        return discussDao.updateScore(id,score);
    }
}
