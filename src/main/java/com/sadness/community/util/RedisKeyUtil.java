package com.sadness.community.util;

/**
 * @version 1.0
 * @Date 2022/6/14 11:25
 * @Author SadAndBeautiful
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";

    // 某个实体的赞
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + ":" + entityType + ":" + entityId;
    }

    // 某个用户的赞
    // like:user:userId -> set(userId)
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + ":" + userId;
    }

    // 用户关注的某个实体
    // followee:userId:entityType -> zset(entityId, now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体的粉丝
    // follower:entityType:rntityId -> zset(userId, now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 获取存储验证码的key
    // kaptcha:kaptchOwner
    public static String getKaptchaKey(String kaptchOwner) {
        return PREFIX_KAPTCHA + SPLIT + kaptchOwner;
    }

    // 获取登陆凭证的key
    // ticket:ticket
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 获取用户的Key
    // user:userId
    public static String getUserKey(int userId) {
        return  PREFIX_USER + SPLIT + userId;
    }

    // 获取单日的uv
    // uv:data
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 获取区间的uv
    // uv:startDate:endDate
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 获取单日的dau
    // dau:data
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 获取区间的dau
    // dau:startDate:endDate
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    // 获取帖子分数的key
    // post:score
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }

}
