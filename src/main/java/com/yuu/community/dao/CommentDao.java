package com.yuu.community.dao;

import com.yuu.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentDao {
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);
    int selectCountByEntity(int entityType, int entityId);
    int insertComment(Comment comment);}
