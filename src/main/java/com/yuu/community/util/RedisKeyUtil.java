package com.yuu.community.util;

public class RedisKeyUtil {
    private static final String SPLIT=":";
    //存帖子和评论的赞
    private static final String PREFIX_ENTITY_LIKE="like:entity";
    //以user为key，记录被点赞的数量！也就是收到多少个赞
    private static final String PREFIX_USER_LIKE="like:user";
    //A关注了B，则A是B的follower（粉丝），B是A的followee（目标）
    private static final String PREFIX_FOLLOWEE="followee";
    //粉丝
    private static final String PREFIX_FOLLOWER="follower";

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

    //某个用户关注的实体
    //userId关注了哪个实体 ex：用户关注了帖子->(帖子id，关注时间)
    //followee:userId:entityType  ->zset(entityId,now)关注的当前时间作为分数
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE+SPLIT+userId+SPLIT+entityType;
    }

    //某个实体拥有的粉丝
    //entityType:entityId明确了是哪个实体   ex：帖子：帖子id ->(哪个用户关注的，关注的时间)
    //follower:entityType:entityId  ->zset(userId,now)关注的当前时间作为分数
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER+SPLIT+entityType+SPLIT+entityId;
    }
}
