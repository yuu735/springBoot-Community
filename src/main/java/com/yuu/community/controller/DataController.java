package com.yuu.community.controller;

import com.yuu.community.service.Impl.DataService;
import com.yuu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class DataController {
    @Autowired
    private DataService dataService;
    @Autowired
    private HostHolder hostHolder;

    //打开统计页面
    @RequestMapping(path = "/data",method ={ RequestMethod.GET, RequestMethod.POST})
    public String getDataPage(){
        return "site/admin/data";
    }
    //统计网站uv请求
    @RequestMapping(path = "/data/uv",method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model){
        long uv=dataService.calculateUV(start,end);
        model.addAttribute("uvResult",uv);
        model.addAttribute("uvStartDate",start);
        model.addAttribute("uvEndDate",end);
        //处理一半后剩下交给平级的请求处理
        return "forward:/data";
    }
    //统计网站dau请求
    @RequestMapping(path = "/data/dau",method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model){
        long uv=dataService.calculateDAU(start,end);
        model.addAttribute("dauResult",uv);
        model.addAttribute("dauStartDate",start);
        model.addAttribute("dauEndDate",end);
        return "forward:/data";
    }


}
