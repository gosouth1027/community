package com.sadness.community.dao;

import com.sadness.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * @version 1.0
 * @Date 2022/6/10 9:25
 * @Author SadAndBeautiful
 */
@Mapper
public interface CommentMapper {

    /**
     * 根据实体类型和id查询所有评论，并分页
     */
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    /**
     * 根据实体类型和id查询评论数量
     */
    int selectCommentCountByEntity(int entityType, int entityId);

    /**
     * 添加评论
     */
    int insertComment(Comment comment);

    /**
     * 根据id查询评论
     */
    Comment selectCommentById(int id);

}
