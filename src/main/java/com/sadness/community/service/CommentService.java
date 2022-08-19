package com.sadness.community.service;

import com.sadness.community.dao.CommentMapper;
import com.sadness.community.entity.Comment;
import com.sadness.community.util.CommunityConstant;
import com.sadness.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @version 1.0
 * @Date 2022/6/10 10:37
 * @Author SadAndBeautiful
 */
@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    /**
     * 根据实体类型查询所有帖子，并分页
     */
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    /**
     * 根据实体类型查询帖子数量
     */
    public int findCommentCountByEntity(int entityType, int entityId) {
        return commentMapper.selectCommentCountByEntity(entityType, entityId);
    }

    /**
     * 添加帖子的评论，使用事务管理，过滤敏感词
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new  IllegalArgumentException("参数不能为空！");
        }

        // 评论信息过滤，html和敏感词
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));

        // 添加评论
        int row = commentMapper.insertComment(comment);

        // 更新帖子回复数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCommentCountByEntity(ENTITY_TYPE_POST, comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return row;
    }

    /**
     * 根据Id查询评论
     */
    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}
