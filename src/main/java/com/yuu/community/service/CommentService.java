package com.yuu.community.service;

import com.yuu.community.entity.Comment;

import java.util.List;

public interface CommentService {
    List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit);
    int findCommentCount(int entityType, int entityId);
    //增加评论(会有两个业务操作因此需要事务管理)
    int addComment(Comment comment);
}
