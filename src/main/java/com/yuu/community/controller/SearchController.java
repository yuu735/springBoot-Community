package com.yuu.community.controller;

import com.yuu.community.entity.DiscussPost;
import com.yuu.community.entity.Page;
import com.yuu.community.service.Impl.ElasticsearchService;
import com.yuu.community.service.LikeService;
import com.yuu.community.service.UserService;
import com.yuu.community.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController {
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;

    @RequestMapping(path="/search",method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) throws IOException {
        //搜索帖子,page里面设置的current是1，但是搜寻是从0开始因此要-1
        List<DiscussPost> searchResult=
        elasticsearchService.searchDiscussPost(keyword,page.getCurrent()-1, page.getLimit());
        //聚合数据
        List<Map<String,Object>> discussPosts=new ArrayList<>();
        if(searchResult!=null){
            for(DiscussPost post:searchResult){
                Map<String,Object> map=new HashMap<>();
                //帖子
                map.put("post",post);
                //作者
                map.put("user",userService.findUserById(post.getUserId()));
                //点赞数量
                map.put("likeCount",likeService.findEntityLikeCount(Constant.ENTITY_TYPE_POST,post.getId()));
                //封装
                discussPosts.add(map);
            }
            model.addAttribute("discussPosts",discussPosts);
            //查询的关键字
            model.addAttribute("keyword",keyword);
            page.setPath("/search?keyword="+keyword);
            page.setRows(searchResult==null? 0 : searchResult.size());

        }
        return "/site/search";
    }
}
