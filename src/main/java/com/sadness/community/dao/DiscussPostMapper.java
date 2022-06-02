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
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    /**
     * 查询记录条数，userId为0时查询所有记录，否则按userId查询
     * @param userId
     * @return
     */
    int selectCount(@Param("userId") int userId);

}
