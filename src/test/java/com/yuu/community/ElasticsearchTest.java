package com.yuu.community;

import com.alibaba.fastjson.JSONObject;
import com.yuu.community.dao.DiscussDao;
import com.yuu.community.dao.elasticsearch.DiscussPostRepository;
import com.yuu.community.entity.DiscussPost;
import com.yuu.community.util.MailClient;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTest {
    @Autowired
    private DiscussPostRepository discussPostRepository;
    @Autowired
    private DiscussDao discussDao;
    @Qualifier("client")
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void testinsert(){
        //如果发现索引没有就自动创建索引加入数据
        discussPostRepository.save(discussDao.selectDiscussPostById(241));
        discussPostRepository.save(discussDao.selectDiscussPostById(242));
        discussPostRepository.save(discussDao.selectDiscussPostById(243));

    }
    @Test
    public void testinsertList(){
        //如果发现索引没有就自动创建索引加入数据
        discussPostRepository.saveAll(discussDao.selectDiscussPosts(153,0,100));
        discussPostRepository.saveAll(discussDao.selectDiscussPosts(101,0,100));
        discussPostRepository.saveAll(discussDao.selectDiscussPosts(102,0,100));
        discussPostRepository.saveAll(discussDao.selectDiscussPosts(103,0,100));
        discussPostRepository.saveAll(discussDao.selectDiscussPosts(111,0,100));
        discussPostRepository.saveAll(discussDao.selectDiscussPosts(112,0,100));
        discussPostRepository.saveAll(discussDao.selectDiscussPosts(131,0,100));
        discussPostRepository.saveAll(discussDao.selectDiscussPosts(132,0,100));
        discussPostRepository.saveAll(discussDao.selectDiscussPosts(133,0,100));
        discussPostRepository.saveAll(discussDao.selectDiscussPosts(134,0,100));



    }
    //修改某条索引数据
    @Test
    public void testUpdate(){
        DiscussPost post=discussDao.selectDiscussPostById(281);
        post.setContent("我是修改过后的内容！");
        discussPostRepository.save(post);
    }
    //删除
    @Test
    public void testDelete(){
        discussPostRepository.deleteById(238);
    }
    //搜寻
    //不带高亮的查询
    @Test
    public void testSearchRepository() throws IOException {
        //可以选择是否要高亮搜寻-操作是给关键词前后加一个高亮标签！
        SearchRequest searchRequest = new SearchRequest("discusspost");//discusspost是索引名，就是表名
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                //在discusspost索引的title和content字段中都查询“互联网寒冬”
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                //这里使用模糊查询
                    // matchQuery是模糊查询，会对key进行分词：searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));
                    // termQuery是精准查询：searchSourceBuilder.query(QueryBuilders.termQuery(key,value));
                //按照什么排序
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC)) //置顶在前面的话就是用倒序
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))    //有无加精，分数高的就是有加精
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))   //最后按照创建时间最新的在最前
                //一个可选项，用于控制允许搜索的时间：searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                .from(0)// 指定从哪条开始查询    对应es6的page
                .size(10);// 需要查出的总记录条数

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println(JSONObject.toJSON(searchResponse));

        List<DiscussPost> list = new LinkedList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
            System.out.println(discussPost);
            list.add(discussPost);
        }

    }
    //带高亮的查询
    //将加入标签实现高亮的修改结果，覆盖原来的数据内容！
    @Test
    public void highlightQuery() throws Exception {
        SearchRequest searchRequest = new SearchRequest("discusspost");//discusspost是索引名，就是表名

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(0)// 指定从哪条开始查询
                .size(10)// 需要查出的总记录条数
                .highlighter(highlightBuilder);//高亮

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<DiscussPost> list = new LinkedList<>();
        //这里将有高亮的结果覆盖原来的数据！！
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
            // 处理高亮显示的结果
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                discussPost.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                discussPost.setContent(contentField.getFragments()[0].toString());
            }
            System.out.println(discussPost);
            list.add(discussPost);
        }
    }



}
