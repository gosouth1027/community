package com.sadness.community.dao;

import com.sadness.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @version 1.0
 * @Date 2022/5/31 16:39
 * @Author SadAndBeautiful
 */

@Mapper
public interface DiscussPostMapper {

    /**
     * 查询帖子，userid为0查询所有，否则按id查询
     */
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit,
                                         @Param("orderMode")  int orderMode);

    /**
     * 查询记录条数，userId为0时查询所有记录，否则按userId查询
     */
    int selectCount(@Param("userId") int userId);

    /**
     * 添加帖子
     */
    int insertDiscussPost(DiscussPost post);

    /**
     * 根据帖子id查询帖子
     */
    DiscussPost getDiscussPostById(@Param("id") int id);

    /**
     * 更新帖子评论数量
     */
    int updateCommentCount(int commentId, int count);

    /**
     * 修改帖子类型  0：普通，1：置顶
     */
    int updateType(int id, int type);

    /**
     * 修改帖子状态  0：正常 1：加精 2：拉黑
     */
    int updateStatus(int id, int status);

    /**
     * 更新帖子分数
     */
    int updateScore(int id, double score);

}
