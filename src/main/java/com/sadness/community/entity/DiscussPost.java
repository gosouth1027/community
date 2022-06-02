package com.sadness.community.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @version 1.0
 * @Date 2022/5/31 16:19
 * @Author SadAndBeautiful
 */


/**
 * 帖子
 */
@Data
@ToString
public class DiscussPost {

    private int id;
    private int userId;
    private String title;
    private String content;
    private int type;
    private int status;
    private Date createTime;
    private int commentCount;
    private double score;

}
