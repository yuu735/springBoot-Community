package com.yuu.community;


import com.yuu.community.entity.DiscussPost;
import com.yuu.community.entity.User;
import com.yuu.community.service.DiscussPostService;
import com.yuu.community.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;


@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserService userMapper;

    @Autowired
    private DiscussPostService discussPostMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.findUserById(101);
        System.out.println(user);

        User user2 = userMapper.findUserByName("liubei");
        System.out.println(user2);

        User user3 = userMapper.findUserByEmail("nowcoder101@sina.com");
        System.out.println(user3);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.addUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void updateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "hello");
        System.out.println(rows);
    }

    @Test
    public void testSelectPosts() {
        List<DiscussPost> list = discussPostMapper.findDiscussPosts(149, 0, 10);
        for(DiscussPost post : list) {
            System.out.println(post);
        }

        int rows = discussPostMapper.findDiscussPostRows(149);
        System.out.println(rows);
    }
}
