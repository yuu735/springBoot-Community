package com.yuu.community.util;

public class Constant {
    /**
     * 激活成功
     */
    public static final int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    public static final int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    public static final int ACTIVATION_FAILURE = 2;
    /**
     * 默认状态的登录凭证的超时时间，没有选择
     */
    public static final int DEFAULT_EXPIRED_SECONDS=3600*12;

    /**
     * 记住状态下的登录凭证超时时间，选择记住我
     */
    public static final int REMEMBER_EXPIRED_SECONDS=3600*24*100;

    /**
     * 回复的实体类型：帖子（评论回复帖子）
     */
    public static final int ENTITY_TYPE_POST=1;
    /**
     * 回复的实体类型：评论（评论回复评论）
     */
    public static final int ENTITY_TYPE_COMMENT=2;
    /**
     * 回复的实体类型：用户
     */
    public static final int ENTITY_TYPE_USER=3;
    /**
     * 主题发帖
     */
    public static final String TOPIC_PUBLISH="publish";
    /**
     * 主题: 评论
     */
    public static final String TOPIC_COMMENT = "comment";

    /**
     * 主题: 点赞
     */
    public static final String TOPIC_LIKE = "like";

    /**
     * 主题: 关注
     */
    public static final String TOPIC_FOLLOW = "follow";
    /**
     * 主题: 删除
     */
    public static final String TOPIC_DELETE = "delete";


    /**
     * 系统用户ID
     */
    public static final int SYSTEM_USER_ID = 1;
    /**
     * 权限：普通用户
     */
    public static final String AUTHORITY_USER="user";
    /**
     * 权限：管理员
     */
    public static final String AUTHORITY_ADMIN="admin";
    /**
     * 权限：版主
     */
    public static final String AUTHORITY_MODERATOR="moderator";
}
