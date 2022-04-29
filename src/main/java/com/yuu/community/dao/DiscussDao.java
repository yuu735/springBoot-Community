package com.yuu.community.dao;

import com.yuu.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子
 */
@Mapper
public interface DiscussDao {
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    int selectDiscussPostRows(@Param("userId") int userId);
    //发布帖子
    int insertDiscussPost(DiscussPost discussPost);
    //查询指定帖子
    DiscussPost selectDiscussPostById(int id);
    //更新帖子回复的数量
    int updateCommentCount(int id, int commentCount);
}
