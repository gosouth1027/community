package com.sadness.community.dao;

import com.sadness.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @Date 2022/5/31 11:29
 * @Author SadAndBeautiful
 */

@Mapper
public interface UserMapper {

    User selectUserById(@Param("id") int id);

    User selectUserByUsername(@Param("username") String username);

    User selectUserByEmail(@Param("email") String email);

    int insertUser(User user);

    int updateStatus(@Param("id") int id, @Param("status") int status);

    int updateHeader(@Param("id") int id, @Param("headerUrl") String headerUrl);

    int updatePassword(@Param("id") int id, @Param("password") String password);
}
