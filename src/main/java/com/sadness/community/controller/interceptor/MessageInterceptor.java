package com.sadness.community.controller.interceptor;

import com.mysql.cj.Messages;
import com.sadness.community.entity.User;
import com.sadness.community.service.MessageService;
import com.sadness.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @version 1.0
 * @Date 2022/6/17 19:25
 * @Author SadAndBeautiful
 */
@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            int unreadLetterCount = messageService.findLetterUnreadCount(user.getId(), null);
            int unreadNoticeCount = messageService.findNoticeUnread(user.getId(), null);
            modelAndView.addObject("totalUnreadCount", unreadNoticeCount + unreadLetterCount);
        }
    }
}
