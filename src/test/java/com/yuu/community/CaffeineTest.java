package com.yuu.community;

import com.yuu.community.entity.DiscussPost;
import com.yuu.community.service.Impl.DiscussPostServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTest {
    @Autowired
    private DiscussPostServiceImpl discussPostService;
    @Test
    public void test(){
        for (int i = 0; i < 3000; i++) {
            DiscussPost post=new DiscussPost();
            post.setUserId(111);
            post.setTitle("互联网求职暖春计划");
            post.setContent("21届毕业生看过来，今年的就业情势虽然受到多方面影响，但仍然有好前景！");
            post.setCreateTime(new Date());
            post.setScore(Math.random()*2000);
            discussPostService.addDiscussPost(post);
        }
    }
}
