package com.likeit.search.controller;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mafeichao
 */
@Slf4j
@RestController
@RequestMapping("/search")
public class SearchController {
    private static String DOC_INDEX = "likeit_docs";

    @Resource
    private RestHighLevelClient esClient;

    class RankItem {
        public String rank;
        public String title;
        public String summary;
        public String url;
        public String add_time;
    }

    private SearchResponse searchV0(String q, int page) {
        SearchRequest searchRequest = new SearchRequest(DOC_INDEX);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(q, "title", "content", "html_text", "query", "tags", "summary", "src");
        searchSourceBuilder.query(queryBuilder);

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title").field("content").field("html_text")
                .preTags("<font color='red'>").postTags("</font>");

        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.from(page * 10);
        searchSourceBuilder.size(10);

        searchRequest.source(searchSourceBuilder);
        log.info("search index {}, {}, dsl:{}", DOC_INDEX, q, searchSourceBuilder.toString());
        try {
            SearchResponse response = esClient.search(searchRequest, RequestOptions.DEFAULT);
            log.info("search succeed:{}, {}, {}", DOC_INDEX, q, response.toString());
            return response;
        } catch (IOException e) {
            log.info("search failed:{}, {}, {}, {}", DOC_INDEX, q, e.getMessage(), e.getStackTrace());
            return null;
        }
    }

    @GetMapping("/query.json")
    public Object query(@RequestParam String q, @RequestParam int page, @RequestParam(required = false, defaultValue = "v0") String algo) {
        Map<String, Object> result = new HashMap<>();

        SearchResponse response = null;
        switch (algo) {
            case "v0":
                response = searchV0(q, page);
                break;
            case "v1":
                break;
            default:
                response = searchV0(q, page);
        }

        List<RankItem> list = new ArrayList<>();
        result.put("data", list);
        if(response == null) {
            return result;
        }

        int rank = 0;
        for(SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> data = hit.getSourceAsMap();
            RankItem item = new RankItem();
            item.rank = String.format("rank:%s, score:%.3f", rank++, hit.getScore());
            item.url = data.get("url").toString();
            item.add_time = data.get("add_time").toString();

            String htmlText = data.get("html_text").toString();

            Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
            HighlightField titleField = highlightFieldMap.get("title");

            if(titleField != null) {
                Text[] frags = titleField.getFragments();
                item.title = String.join("", Arrays.stream(frags).map(x->x.toString()).collect(Collectors.toList()));
            } else {
                String title = data.get("title").toString();
                if(title == null || title.length() == 0) {
                    int len = Math.min(30, htmlText.length());
                    title = htmlText.substring(0, len);
                }
                item.title = title;
            }

            HighlightField contentField = highlightFieldMap.get("content");
            if(contentField != null) {
                Text[] frags = contentField.getFragments();
                item.summary = String.join("", Arrays.stream(frags).map(x->x.toString()).collect(Collectors.toList()));
            } else {
                HighlightField htmlField = highlightFieldMap.get("html_text");
                if(htmlField != null) {
                    Text[] frags = htmlField.getFragments();
                    item.summary = String.join("", Arrays.stream(frags).map(x->x.toString()).collect(Collectors.toList()));
                } else {
                    int len = Math.min(200, htmlText.length());
                    item.summary = htmlText.substring(0, len);
                }
            }

            list.add(item);
        }

        result.put("nav", String.format("total:%s, page:%s, size:10", response.getHits().getTotalHits().value, page));
        return result;
    }
}
