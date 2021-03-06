package com.yuu.community.dao;

import com.yuu.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentDao {
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);
    int selectCountByEntity(int entityType, int entityId);
    int insertComment(Comment comment);
    Comment selectCommentById(int id);
    List<Comment> selectCommentsByUserid(int userId, int offset, int limit);
    int selectCommentCountByUserid(int userId);
    // 修改状态
    int updateStatus(List<Integer> ids,int status);
    List<Comment> selectCommentsByTypeAndId(int entityType, int entityId);
}
