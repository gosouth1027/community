package com.sadness.community.util;

import com.sadness.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @Date 2022/6/7 16:41
 * @Author SadAndBeautiful
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }

}
