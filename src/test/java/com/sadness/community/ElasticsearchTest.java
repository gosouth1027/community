package com.sadness.community;

import com.alibaba.fastjson.JSONObject;
import com.sadness.community.dao.DiscussPostMapper;
import com.sadness.community.dao.elasticsearch.DiscussPostRepository;
import com.sadness.community.entity.DiscussPost;
import com.sadness.community.service.ElasticsearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2022/6/19 17:07
 * @Author SadAndBeautiful
 */
@SpringBootTest
public class ElasticsearchTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Test
    public void test() throws IOException {
        Map<String, Object> map = elasticsearchService.searchDiscussPost("大厂", 0, 10);
        if (map != null) {
            List<DiscussPost> list = (List<DiscussPost>) map.get("discussPosts");
        }
    }

    @Test
    public void testInsert() {
        discussPostRepository.save(discussPostMapper.getDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.getDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.getDiscussPostById(243));
    }

    @Test
    public void testInsertList() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100, 0));
    }

    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.getDiscussPostById(231);
        post.setContent("这是我一次发帖");
        discussPostRepository.save(post);
    }

    @Test
    public void testDelete() {
        discussPostRepository.deleteById(231);
    }

    //不带高亮的查询
    @Test
    public void noHighlightQuery() throws IOException {
        // 创建查询请求，指定索引（表名）
        SearchRequest searchRequest = new SearchRequest("discusspost");

        // 构造搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(0)
                .size(10);

        // 填入查询条件
        searchRequest.source(searchSourceBuilder);
        //
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(JSONObject.toJSON(search));
        System.out.println("======================================");
        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit hit : search.getHits().getHits()) {
            DiscussPost post = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
            System.out.println(post);
            list.add(post);
        }
        System.out.println("======================================");
    }

    //带高亮的查询
    @Test
    public void highlightQuery() throws IOException {
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
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .highlighter(highlightBuilder)
                .from(0)
                .size(10);

        // 填入查询条件，并查询
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println(search.getHits().getTotalHits());
        System.out.println(search.getHits().getMaxScore());
        System.out.println(search.getAggregations());
        System.out.println(search.getScrollId());

        System.out.println("======================================");
        List<DiscussPost> list = new ArrayList<>();
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

            System.out.println(post);
            list.add(post);
        }
        System.out.println("======================================");

    }
}
