package com.yuu.community.service.Impl;

import com.yuu.community.dao.CommentDao;
import com.yuu.community.dao.DiscussDao;
import com.yuu.community.entity.Comment;
import com.yuu.community.service.CommentService;
import com.yuu.community.service.DiscussPostService;
import com.yuu.community.util.Constant;
import com.yuu.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Autowired
    private DiscussPostService discussPostService;

    @Override
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentDao.selectCommentsByEntity(entityType,entityId,offset,limit);
    }

    @Override
    public int findCommentCount(int entityType, int entityId) {
        return commentDao.selectCountByEntity(entityType,entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    @Override
    public int addComment(Comment comment) {
        if(comment==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        //新增的评论存到数据库
        int rows=commentDao.insertComment(comment);
        //更新针对帖子的评论数量（如果是对评论的回复则不会增加回帖数量）
        if(comment.getEntityType()== Constant.ENTITY_TYPE_POST){
            int count=commentDao.selectCountByEntity(comment.getEntityType(),comment.getEntityId());
            //discuss_post表中的commentCount是根据查询comment表中的对应分组数量(sql语句查询)得到的数值
            discussPostService.updateCommentCount(comment.getEntityId(),count);//更新
        }
        return rows;
    }
}
