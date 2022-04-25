package com.yuu.community.dao;

import com.yuu.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketDao {
    //因为要换行所以用{}将所有包裹起来到时候好拼接在一起
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);
        //使用了动态sql<if>，需要在外面加一个脚本<script>才可以用<if>,这里只是演示怎么用
        @Update({
                "<script>",
                "update login_ticket set status=#{status} where ticket=#{ticket} ",
                "<if test=\"ticket!=null\"> ",
                "and 1=1 ",
                "</if>",
                "</script>"
        })
        int updateStatus(String ticket, int status);

}
