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

}
