package com.likeit.index;

import com.alibaba.fastjson.JSON;
import com.likeit.pa.PageAttribute;
import com.likeit.pa.PageInfo;
import com.likeit.utils.Tools;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * @author mafeichao
 */
public class Indexer {
    static String INDEX = "likeit_htmls";
    static String INDEX_DOC = "likeit_docs";
    static Logger LOG = LoggerFactory.getLogger(Indexer.class);
    static RestHighLevelClient esClient;

    public Indexer() {
        esClient = new RestHighLevelClient(RestClient.builder(new HttpHost("82.157.160.66",9200)));
    }

    public void indexByUrl(String url) {
        Map<String, Object> data = new HashMap<>();

        try {
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.termQuery("url", url));

            SearchRequest request = new SearchRequest(INDEX);
            request.source(builder);

            SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);
            LOG.info("search dsl:{}", builder.toString());
            SearchHit[] hits = response.getHits().getHits();
            if(hits.length > 0) {
                String html = null;
                Set<String> srcs = new HashSet<>();
                Set<String> querys = new HashSet<>();
                Set<String> add_time = new HashSet<>();
                for(SearchHit hit : hits) {
                    Map<String, Object> hitData = hit.getSourceAsMap();
                    String dt = hitData.get("html").toString();
                    if(html == null || dt.length() > html.length()) {
                        html = dt;
                    }

                    String src = hitData.get("src").toString();
                    srcs.add(src);

                    String query = hitData.get("query").toString();
                    querys.add(query);

                    String time = hitData.get("add_time").toString();
                    add_time.add(time);
                }

                data.put("url", url);
                data.put("src", srcs);
                data.put("query", querys);
                data.put("add_time", add_time);

                PageInfo info = PageAttribute.extract(html, url);
                if(info != null) {
                    data.put("title", info.title);
                    data.put("content", info.content);
                    data.put("html_words", info.doc.text());
                } else {
                    data.put("title", "");
                    data.put("content", "");
                    data.put("html_words", "");
                }
            }
        } catch (Exception e) {
            LOG.error("search failed:{},{},{}", JSON.toJSONString(Tools.subMapString(data, 30)), e.getMessage(), e.getStackTrace());
            return;
        }

        String id = DigestUtils.md5Hex(url);
        IndexRequest request = new IndexRequest();
        request.index(INDEX_DOC).id(id).source(data).timeout(TimeValue.timeValueSeconds(1));
        try {
            IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);
            LOG.info("index succeed:{},{}", JSON.toJSONString(Tools.subMapString(data, 30)), response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("index failed:{},{},{}", JSON.toJSONString(Tools.subMapString(data, 30)), e.getMessage(), e.getStackTrace());
        }
    }

    public void indexAll() {
        try {
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.matchAllQuery());

            Scroll scroll = new Scroll(TimeValue.timeValueMillis(1));

            SearchRequest request = new SearchRequest(INDEX);
            request.source(builder);
            request.scroll(scroll);

            LOG.info("search dsl:" + request.toString());

            SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);

            SearchHit[] hits = response.getHits().getHits();
            String scrollId = response.getScrollId();
            while(hits != null && hits.length > 0) {
                for(SearchHit hit : hits) {

                }

                SearchScrollRequest sRequest = new SearchScrollRequest(scrollId);
                sRequest.scroll(scroll);

                try {
                    response = esClient.scroll(sRequest, RequestOptions.DEFAULT);
                    hits = response.getHits().getHits();
                    scrollId = response.getScrollId();
                } catch (Exception e) {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Indexer indexer = new Indexer();
        indexer.indexByUrl("http://www.cnblogs.com/CongZhang/p/5944463.html");
    }
}
