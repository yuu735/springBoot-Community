package com.yuu.community.controller;

import com.yuu.community.entity.DiscussPost;
import com.yuu.community.entity.Page;
import com.yuu.community.entity.User;
import com.yuu.community.service.DiscussPostService;
import com.yuu.community.service.LikeService;
import com.yuu.community.service.MessageService;
import com.yuu.community.service.UserService;
import com.yuu.community.util.Constant;
import com.yuu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    //查询帖子的点赞数量进行初始化
    @Autowired
    private LikeService likeService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;


    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page,@RequestParam(name="orderMode",defaultValue = "0") int orderMode) {
        page.setRows(discussPostService.findDiscussPostRows(0));
        //翻页的时候不能把orderMode给丢了所以要拼进去链接,这样才能带上orderMode
        page.setPath("/index?orderMode="+orderMode);
        //orderMode=0 默认发帖时间倒序，orderMode=1 按分数热度倒序
        List<DiscussPost> list=discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit(),orderMode);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                long likeCount=likeService.findEntityLikeCount(Constant.ENTITY_TYPE_POST,post.getId());
                map.put("likeCount",likeCount);
                discussPosts.add(map);
            }
        }

        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("page",page);
        model.addAttribute("orderMode",orderMode);
        return "index";

    }
    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String getErroerPage(){
        return "error/500";
    }
    //权限不足跳转这
    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "error/404";
    }

}
