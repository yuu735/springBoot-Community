package com.yuu.community.service;

import com.yuu.community.entity.DiscussPost;

import java.util.List;

public interface DiscussPostService {
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit);
    public int findDiscussPostRows(int userId);
}
