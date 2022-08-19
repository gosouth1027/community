package com.sadness.community.util;

/**
 * @version 1.0
 * @Date 2022/6/5 19:59
 * @Author SadAndBeautiful
 */
public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 登陆凭证默认的超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 登陆凭证记住的超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 60;

    /**
     * 实体类型：帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型：评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型：用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 主题类型：评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题类型：点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题类型：关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 主题类型：发帖
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 主题类型：删帖
     */
    String TOPIC_DELETE = "delete";

    /**
     * 主题类型：分享
     */
    String TOPIC_SHARE = "share";

    /**
     * 系统用户Id
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 权限：普通用户
     */
    String AUTHORITY_USER = "user";

    /**
     * 权限：管理员
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     * 权限：版主
     */
    String AUTHORITY_MODERATOR = "moderator";

}
