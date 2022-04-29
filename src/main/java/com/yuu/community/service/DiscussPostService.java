package com.yuu.community.service;

import com.yuu.community.entity.DiscussPost;

import java.util.List;

public interface DiscussPostService {
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit);
    public int findDiscussPostRows(int userId);
    //保存发布的帖子
    int addDiscussPost(DiscussPost post);
    //查看指定的帖子
    DiscussPost findDiscussPostById(int id);
    //更新回复数量
    int updateCommentCount(int id, int commentCount);
}
