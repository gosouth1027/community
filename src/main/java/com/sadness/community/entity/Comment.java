package com.sadness.community.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @version 1.0
 * @Date 2022/6/10 9:22
 * @Author SadAndBeautiful
 */

@Data
@ToString
public class Comment {

    private int id;
    private int userId;
    private int entityType;
    private int entityId;
    private int targetId;
    private String content;
    private int status;
    private Date createTime;

}
