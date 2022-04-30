package com.yuu.community.util;

public class RedisKeyUtil {
    private static final String SPLIT=":";
    //存帖子和评论的赞
    private static final String PREFIX_ENTITY_LIKE="like:entity";
    //以user为key，记录被点赞的数量！也就是收到多少个赞
    private static final String PREFIX_USER_LIKE="like:user";

    /**
     * 用于生成Redis中的键->某个实体的赞(key)
     * key格式-> like:entity:entityType:entityId
     * value放什么->set(userId) 谁给这个实体点了赞就记录这个id存到set集合中
     * @param entityType    某个实体的类型（帖子or评论）
     * @param entityId      某个实体的id
     * @return  存入到redis中的 key
     */


    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }

    //某个用户收到的赞
    //like:user:userId  ->存的value是int(点赞数量)
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE+SPLIT+userId;
    }

}
