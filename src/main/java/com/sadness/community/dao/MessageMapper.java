package com.sadness.community.dao;

import com.sadness.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @version 1.0
 * @Date 2022/6/12 15:35
 * @Author SadAndBeautiful
 */
@Mapper
public interface MessageMapper {

    /**
     * 查询用户所有会话，只显示最新的一条消息，分页显示
     */
    List<Message> selectConversationsByUserId(int userId, int offset, int limit);

    /**
     * 查询用户会话数量
     */
    int selectConversationCountByUserId(int userId);

    /**
     * 根据会话查询私信详情
     */
    List<Message> selectLettersByConversation(String conversationId, int offset, int limit);

    /**
     * 根据会话查询私信数量
     */
    int selectLettersCountByConversation(String conversationId);

    /**
     * 查询未读私信条数
     */
    int selectLetterUnreadCount(int userId, String conversationId);

    /**
     * 添加私信
     */
    int insertMessage(Message message);

    /**
     * 读消息
     */
    int updateStatus(List<Integer> ids, int status);

    /**
     * 查询某主题最后一条通知
     */
    Message selectLastNotice(int userId, String topic);

    /**
     * 查询某主题通知的数量
     */
    int selectNoticeCount(int userId, String topic);

    /**
     * 查询某个主题未读消息的数量， topic可以为null，查询所有通知未读数量
     */
    int selectNoticeUnread(int userId, String topic);

    /**
     * 查询某个主题的通知消息列表
     */
    List<Message> selectNoticesList(int userId, String topic, int offset, int limit);
}
