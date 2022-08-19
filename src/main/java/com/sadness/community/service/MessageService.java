package com.sadness.community.service;

import com.sadness.community.dao.MessageMapper;
import com.sadness.community.entity.Message;
import com.sadness.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @version 1.0
 * @Date 2022/6/12 16:58
 * @Author SadAndBeautiful
 */
@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 查询用户所有会话，只显示最新的一条消息，分页显示
     */
    public List<Message> findConversationsByUserId(int userId, int offset, int limit) {
        return messageMapper.selectConversationsByUserId(userId, offset, limit);
    }

    /**
     * 查询用户会话数量
     */
    public int findConversationCountByUserId(int userId) {
        return messageMapper.selectConversationCountByUserId(userId);
    }

    /**
     * 根据会话查询私信详情
     */
    public List<Message> findLettersByConversation(String conversationId, int offset, int limit) {
        return messageMapper.selectLettersByConversation(conversationId, offset, limit);
    }

    /**
     * 根据会话查询私信数量
     */
    public int findLettersCountByConversation(String conversationId) {
        return messageMapper.selectLettersCountByConversation(conversationId);
    }

    /**
     * 查询未读私信条数
     */
    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    /**
     * 添加私信
     */
    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    /**
     * 读消息
     */
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }

    /**
     * 查询某主题最后一条通知
     */
    public Message findLastNotice(int userId, String topic) {
        return messageMapper.selectLastNotice(userId, topic);
    }

    /**
     * 查询某主题通知的数量
     */
    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    /**
     * 查询某个主题未读消息的数量， topic可以为null，查询所有通知未读数量
     */
    public int findNoticeUnread(int userId, String topic) {
        return messageMapper.selectNoticeUnread(userId, topic);
    }

    /**
     * 查询某个主题的通知消息列表
     */
    public List<Message> findNoticesList(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNoticesList(userId, topic, offset, limit);
    }

}
