package com.yuu.community.service.Impl;

import com.yuu.community.dao.DiscussDao;
import com.yuu.community.entity.DiscussPost;
import com.yuu.community.service.DiscussPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostServiceImpl implements DiscussPostService {
    @Autowired
    private DiscussDao discussDao;
    @Override
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussDao.selectDiscussPosts(userId,offset,limit);
    }

    @Override
    public int findDiscussPostRows(int userId) {
        return discussDao.selectDiscussPostRows(userId);
    }
}
