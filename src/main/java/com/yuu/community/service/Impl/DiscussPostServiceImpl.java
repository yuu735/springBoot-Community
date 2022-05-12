package com.yuu.community.service.Impl;

import com.yuu.community.dao.DiscussDao;
import com.yuu.community.entity.DiscussPost;
import com.yuu.community.service.DiscussPostService;
import com.yuu.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostServiceImpl implements DiscussPostService {
    @Autowired
    private DiscussDao discussDao;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Override
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussDao.selectDiscussPosts(userId,offset,limit);
    }

    @Override
    public int findDiscussPostRows(int userId) {
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
}
