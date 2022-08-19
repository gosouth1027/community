package com.sadness.community.dao.elasticsearch;

import com.sadness.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @version 1.0
 * @Date 2022/6/19 16:59
 * @Author SadAndBeautiful
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {

}
