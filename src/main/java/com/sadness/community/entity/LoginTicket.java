package com.sadness.community.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @version 1.0
 * @Date 2022/6/6 15:59
 * @Author SadAndBeautiful
 */
@Data
@ToString
public class LoginTicket {

    private int id;

    private int userId;

    private String ticket;

    private int status;

    private Date expired;

}
