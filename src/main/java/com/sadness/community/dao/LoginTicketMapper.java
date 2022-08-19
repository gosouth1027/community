package com.sadness.community.dao;

import com.sadness.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * @version 1.0
 * @Date 2022/6/6 16:01
 * @Author SadAndBeautiful
 */
@Mapper
@Deprecated
public interface LoginTicketMapper {

    /**
     * 添加凭证
     */
    @Insert({
            "insert into login_ticket(user_id, ticket, status, expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    /**
     * 查询凭证
     */
    @Select({
            "select id, user_id, ticket, status, expired from login_ticket ",
            "where ticket = #{ticket}"
    })
    LoginTicket selectLoginTicket(String ticket);

    /**
     * 修改凭证状态
     */
    @Update({
            "update login_ticket set status = #{status} where ticket = #{ticket}"
    })
    int updateLoginTicket(String ticket, int status);

}
