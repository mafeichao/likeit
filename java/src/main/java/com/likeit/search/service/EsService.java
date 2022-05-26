package com.likeit.search.service;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EsService {
    @Resource
    private RestHighLevelClient esClient;

    public boolean indexData(String index, Map<String, Object> data) {
        if(data.isEmpty()) {
            return false;
        }

        IndexRequest indexRequest = new IndexRequest();

        if(data.containsKey("id")) {
            String id = data.get("id").toString();
            indexRequest.index(index).id(id).source(data);
        } else {
            indexRequest.index(index).source(data);
        }

        try {
            IndexResponse indexResponse = esClient.index(indexRequest, RequestOptions.DEFAULT);
            log.info("index succeed:{}", indexResponse);
            return true;
        } catch (IOException e) {
            log.error("index failed:{},{}", e.getMessage(), e.getStackTrace());
            return false;
        }
    }

    public List<String> strEQStr(String index, String qKey, String qValue, String oKey) {
        List<String> result = new ArrayList<>();

        SearchRequest searchRequest = new SearchRequest(index);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery(qKey, qValue));

        searchRequest.source(searchSourceBuilder);
        log.info("search dsl:{}", searchSourceBuilder.toString());

        try {
            SearchResponse response = esClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            if(hits != null && hits.length > 0) {
                for(SearchHit hit : hits) {
                    String oValue = hit.getSourceAsMap().get(oKey).toString();

                    result.add(oValue);
                }
            }
            log.info("search succeed:{}", response.toString());
        } catch (IOException e) {
            log.error("search failed:{},{}", e.getMessage(), e.getStackTrace());
        }

        return result;
    }
}
