package com.sadness.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.sadness.community.dao.MessageMapper;
import com.sadness.community.dao.UserMapper;
import com.sadness.community.entity.Message;
import com.sadness.community.entity.Page;
import com.sadness.community.entity.User;
import com.sadness.community.service.MessageService;
import com.sadness.community.service.UserService;
import com.sadness.community.util.CommunityConstant;
import com.sadness.community.util.CommunityUtil;
import com.sadness.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @version 1.0
 * @Date 2022/6/12 17:03
 * @Author SadAndBeautiful
 */
@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /**
     * 响应私信列表
     */
    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page) {

        User user = hostHolder.getUser();

        // 设置分页条件
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCountByUserId(user.getId()));

        // 查询会话列表
        List<Message> conversationList =
                messageService.findConversationsByUserId(user.getId(), page.getOffset(), page.getLimit());
        // 使用map对象返回给model
        List<Map<String, Object>> conversations = new ArrayList<>();
        // 遍历会话列表
        if (conversationList != null) {
            for (Message conversation : conversationList) {
                Map<String, Object> map = new HashMap<>();
                // 会话
                map.put("conversation", conversation);
                // 该会话私信数量
                map.put("letterCount", messageService.findLettersCountByConversation(conversation.getConversationId()));
                // 该会话未读私信数量
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), conversation.getConversationId()));
                // 该会话对方的用户
                int targetId = user.getId() == conversation.getFromId() ? conversation.getToId() : conversation.getFromId();
                User target = userService.getUserById(targetId);
                map.put("target", target);

                conversations.add(map);
            }
        }
        // 私信消息总的未读数量
        model.addAttribute("letterUnreadCount", messageService.findLetterUnreadCount(user.getId(), null));
        // 系统消息总的未读数量
        int noticeUnreadCount = messageService.findNoticeUnread(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        model.addAttribute("conversations", conversations);
        return "/site/letter";
    }

    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, Page page) {

        // 设置分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLettersCountByConversation(conversationId));

        // 查询私信详情
        List<Message> letterList = messageService.findLettersByConversation(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();

        if (letterList != null) {
            for (Message letter : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", letter);
                map.put("fromUser", userService.getUserById(letter.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);
        model.addAttribute("target", getLetterTarget(conversationId));

        // 将未读消息状态设置为已读
        List<Integer> letterIds = getLetterIds(letterList);
        if (!letterIds.isEmpty()) {
            messageService.readMessage(letterIds);
        }
        return "/site/letter-detail";
    }

    /**
     * 获取未读消息的id
     */
    private List<Integer> getLetterIds(List<Message> messageList) {
        List<Integer> ids = new ArrayList<>();
        if (messageList != null) {
            for (Message letter : messageList) {
                if (hostHolder.getUser().getId() == letter.getToId() && letter.getStatus() == 0) {
                    ids.add(letter.getId());
                }
            }
        }
        return ids;
    }

    private User getLetterTarget(String conversationId) {
        String[] s = conversationId.split("_");
        int id0 = Integer.parseInt(s[0]);
        int id1 = Integer.parseInt(s[1]);
        if (hostHolder.getUser().getId() == id0) {
            return userService.getUserById(id1);
        } else {
            return userService.getUserById(id0);
        }
    }

    /**
     * 发私信
     */
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendMessage(String toName, String content) {

        User toUser = userService.findUserByName(toName);

        if (toUser == null) {
            return CommunityUtil.getJSONString(1, "发送失败，目标用户不存在！");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(toUser.getId());
        String conversationId = getConversationId(message.getToId(), message.getFromId());
        message.setConversationId(conversationId);
        message.setContent(content);
        message.setCreateTime(new Date());

        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0, "发送成功");
    }

    private String getConversationId(int toId, int fromId) {
        if (toId < fromId) {
            return toId + "_" + fromId;
        } else {
            return fromId + "_" + toId;
        }
    }

    /**
     * 响应通知列表
     */
    @GetMapping("/notice/list")
    public String getNoticeList(Model model) {

        User user = hostHolder.getUser();

        // 查询评论类信息
        Message message = messageService.findLastNotice(user.getId(), TOPIC_COMMENT);
        Map<String, Object> messageVO = new HashMap<>();
        messageVO.put("message", message);
        if (message != null) {
            // 将message中content转换回map
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            // 存储评论的人，实体类型，实体Id
            messageVO.put("user", userService.getUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));
            // 该主题消息总数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count", count);
            // 该主题消息未读数量
            int unread = messageService.findNoticeUnread(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread", unread);
        }
        model.addAttribute("commentNotice", messageVO);

        // 查询点赞类信息
        message = messageService.findLastNotice(user.getId(), TOPIC_LIKE);
        messageVO = new HashMap<>();
        messageVO.put("message", message);
        if (message != null) {
            // 将message中content转换回map
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            // 存储点赞的人，实体类型，实体Id
            messageVO.put("user", userService.getUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));
            // 该主题消息总数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count", count);
            // 该主题消息未读数量
            int unread = messageService.findNoticeUnread(user.getId(), TOPIC_LIKE);
            messageVO.put("unread", unread);
        }
        model.addAttribute("likeNotice", messageVO);

        // 查询关注类信息
        message = messageService.findLastNotice(user.getId(), TOPIC_FOLLOW);
        messageVO = new HashMap<>();
        messageVO.put("message", message);
        if (message != null) {
            // 将message中content转换回map
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            // 存储点赞的人，实体类型，实体Id
            messageVO.put("user", userService.getUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            // 该主题消息总数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", count);
            // 该主题消息未读数量
            int unread = messageService.findNoticeUnread(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread", unread);
        }
        model.addAttribute("followNotice", messageVO);

        // 私信消息总的未读数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        // 系统消息总的未读数量
        int noticeUnreadCount = messageService.findNoticeCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "site/notice";
    }

    /**
     * 响应通知详情页面
     */
    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable("topic") String topic, Model model, Page page) {

        User user = hostHolder.getUser();

        // 设置分页信息
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        // 查询通知信息列表，并保存
        List<Message> noticesList = messageService.findNoticesList(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticesVO = new ArrayList<>();
        if (noticesList != null) {
            for (Message notice : noticesList) {
                Map<String, Object> map = new HashMap<>();
                map.put("notice", notice);
                // 处理消息的内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.getUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知作者
                map.put("fromUser", userService.getUserById(1));
                noticesVO.add(map);
            }
        }
        model.addAttribute("notices", noticesVO);

        // 设置已读
        List<Integer> unreadNoticeIds = getLetterIds(noticesList);
        if (!unreadNoticeIds.isEmpty()) {
            messageService.readMessage(unreadNoticeIds);
        }
        return "site/notice-detail";
    }
}
