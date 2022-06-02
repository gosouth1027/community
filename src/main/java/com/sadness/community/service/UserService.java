package com.sadness.community.service;

import com.sadness.community.dao.UserMapper;
import com.sadness.community.entity.User;

/**
 * @version 1.0
 * @Date 2022/6/1 10:42
 * @Author SadAndBeautiful
 */
public class UserService {

    private UserMapper userMapper;

    public User getUserById(int id) {
        return userMapper.selectUserById(id);
    }
}
