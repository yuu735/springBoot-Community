package com.yuu.community;

import com.yuu.community.util.MailClient;
import com.yuu.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTest {
    @Autowired
    private SensitiveFilter filter;

    @Test
    public void test(){
        String text="这里可以☆赌☆博☆，可以开票，开";
        text=filter.filter(text);
        System.out.println(text);
    }
}
