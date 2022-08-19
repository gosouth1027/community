package com.sadness.community.service;

import com.alibaba.fastjson.JSONObject;
import com.sadness.community.dao.elasticsearch.DiscussPostRepository;
import com.sadness.community.entity.DiscussPost;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2022/6/20 15:17
 * @Author SadAndBeautiful
 */
@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository postRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 将帖子存储到es中，或修改帖子
     */
    public void saveDiscussPost(DiscussPost discussPost) {
        postRepository.save(discussPost);
    }

    /**
     * 删除es中的帖子
     */
    public void deleteDiscussPost(int id) {
        postRepository.deleteById(id);
    }

    /**
     * 查询帖子
     */
    public Map<String, Object> searchDiscussPost(String keyword, int current, int limit) throws IOException {

        // 创建查询请求，指定索引（表名）
        SearchRequest searchRequest = new SearchRequest("discusspost");
        // 创建高亮条件，标题和内容都高亮，关键词前后加标签
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("title")
                .field("content")
                .requireFieldMatch(false)
                .preTags("<span style='color:red'>")
                .postTags("</span>");

        // 构造搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .highlighter(highlightBuilder)
                .from(current)
                .size(limit);

        // 填入查询条件，并查询
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        // 保存查询结果
        List<DiscussPost> list = new ArrayList<>();
        if (search != null) {
            // 遍历处理每个查询到的帖子装入List集合
            for (SearchHit hit : search.getHits().getHits()) {
                DiscussPost post = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
                // 处理高亮
                HighlightField title = hit.getHighlightFields().get("title");
                if (title != null) {
                    post.setTitle(title.getFragments()[0].toString());
                }
                HighlightField content = hit.getHighlightFields().get("content");
                if (content != null) {
                    post.setContent(content.getFragments()[0].toString());
                }
                list.add(post);
            }
        } else {
            return null;
        }

        long total = search.getHits().getTotalHits().value;
        Map<String, Object> map = new HashMap<>();
        map.put("discussPosts", list);
        map.put("total", total);
        return map;
    }
}
