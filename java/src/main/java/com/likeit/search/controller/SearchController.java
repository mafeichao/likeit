package com.likeit.search.controller;

import com.likeit.search.dto.RankItem;
import com.likeit.search.dto.SEResponse;
import com.likeit.search.service.CrawlerService;
import com.likeit.search.service.ResponseService;
import com.likeit.search.utils.Consts;
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
import org.elasticsearch.search.sort.SortOrder;
import org.jsoup.Jsoup;
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
    private static final int PAGE_SIZE = 10;
    private static final int SUMMARY_LEN = 200;

    @Resource
    private RestHighLevelClient esClient;

    private SearchResponse searchV0(String q, int page) {
        SearchRequest searchRequest = new SearchRequest(Consts.DOCS_INDEX);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(q, "title", "content", "html_text", "query", "tags", "summary", "src");
        searchSourceBuilder.query(queryBuilder);

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title").field("content").field("html_text")
                .preTags("<font color='red'>").postTags("</font>");

        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.from((page - 1) * PAGE_SIZE);
        searchSourceBuilder.size(PAGE_SIZE);

        searchRequest.source(searchSourceBuilder);
        log.info("search index {}, {}, dsl:{}", Consts.DOCS_INDEX, q, searchSourceBuilder.toString());
        try {
            SearchResponse response = esClient.search(searchRequest, RequestOptions.DEFAULT);
            //log.info("search succeed:{}, {}, {}", Consts.DOCS_INDEX, q, response.toString());
            log.info("search succeed:{}, {}", Consts.DOCS_INDEX, q);
            return response;
        } catch (IOException e) {
            log.info("search failed:{}, {}, {}, {}", Consts.DOCS_INDEX, q, e.getMessage(), e.getStackTrace());
            return null;
        }
    }

    @GetMapping("/query.json")
    public Object query(@RequestParam String q, @RequestParam int page, @RequestParam(required = false, defaultValue = "v0") String algo) {
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
        if (response == null) {
            return ResponseService.builder().data("data", list)
                    .data("total", 0).build();
        }

        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> data = hit.getSourceAsMap();
            RankItem item = new RankItem();
            item.setUrl(data.get("url").toString());
            item.setAddTime(data.get("add_time").toString());

            String htmlText = data.get("html_text").toString();

            Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
            HighlightField titleField = highlightFieldMap.get("title");

            if (titleField != null) {
                Text[] frags = titleField.getFragments();
                item.setTitle(Arrays.stream(frags).map(Text::toString).collect(Collectors.joining("")));
            } else {
                String title = data.get("title").toString();
                if (title == null || title.length() == 0) {
                    int len = Math.min(30, htmlText.length());
                    title = htmlText.substring(0, len);
                }
                item.setTitle(title);
            }

            HighlightField contentField = highlightFieldMap.get("content");
            if (contentField != null) {
                Text[] frags = contentField.getFragments();
                item.setSummary(Arrays.stream(frags).map(Text::toString).collect(Collectors.joining("")));
            } else {
                HighlightField htmlField = highlightFieldMap.get("html_text");
                if (htmlField != null) {
                    Text[] frags = htmlField.getFragments();
                    item.setSummary(Arrays.stream(frags).map(Text::toString).collect(Collectors.joining("")));
                } else {
                    int len = Math.min(SUMMARY_LEN, htmlText.length());
                    item.setSummary(htmlText.substring(0, len));
                }
            }

            list.add(item);
        }

        long total = response.getHits().getTotalHits().value;
        return ResponseService.builder().data("data", list)
                .data("total", total)
                .data("pages", total / PAGE_SIZE + 1).build();
    }

    @GetMapping("/search.json")
    public Object search(@RequestParam String q, @RequestParam int page, @RequestParam(required = false, defaultValue = "bing") String algo) {
        SEResponse response;
        switch (algo) {
            case "baidu":
                response = CrawlerService.baiduSearch(q, page);
                break;
            case "bing":
                response = CrawlerService.bingSearch(q, page);
                break;
            case "google":
                response = CrawlerService.googleSearch(q, page);
                break;
            default:
                response = CrawlerService.bingSearch(q, page);
                break;
        }

        List<RankItem> list = new ArrayList<>();
        if (response == null || response.getCode() != 200) {
            String msg = response.getMsg();
            return ResponseService.builder().data("data", list)
                    .data("msg", msg)
                    .data("total", 0).build();
        }

        list = response.getList();
        long total = response.getTotal();
        return ResponseService.builder().data("data", list)
                .data("total", total)
                .data("pages", total / PAGE_SIZE + 1).build();
    }

    @GetMapping("/uid_ais.json")
    public Object uidAis(@RequestParam Long uid, @RequestParam int page) {
        SearchResponse response = null;

        SearchRequest searchRequest = new SearchRequest(Consts.DOCS_INDEX);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("uid", uid));
        searchSourceBuilder.from((page - 1) * PAGE_SIZE);
        searchSourceBuilder.size(PAGE_SIZE);
        searchSourceBuilder.sort("add_time", SortOrder.DESC);

        searchRequest.source(searchSourceBuilder);
        log.info("search dsl:{}", searchSourceBuilder.toString());

        try {
            response = esClient.search(searchRequest, RequestOptions.DEFAULT);
            //log.info("search succeed:{}", response.toString());
            log.info("search succeed");
        } catch (IOException e) {
            log.error("search failed:{},{}", e.getMessage(), e.getStackTrace());
        }

        List<RankItem> list = new ArrayList<>();
        if (response == null) {
            return ResponseService.builder().data("data", list)
                    .data("total", 0).build();
        }

        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> data = hit.getSourceAsMap();
            String content = data.get("content").toString();
            String htmlText = data.get("html_text").toString();

            try {
                content = Jsoup.parse(content).text();
                htmlText = Jsoup.parse(htmlText).text();
            } catch (Exception e) {
                e.printStackTrace();
            }

            RankItem item = new RankItem();
            item.setUrl(data.get("url").toString());
            item.setAddTime(data.get("add_time").toString());
            item.setTitle(data.get("title").toString());

            String summary = "";
            if (content != null && !content.isEmpty()) {
                int max = Math.min(SUMMARY_LEN, content.length());
                summary = content.substring(0, max);
            } else if (htmlText != null) {
                int max = Math.min(SUMMARY_LEN, htmlText.length());
                summary = htmlText.substring(0, max);
            }
            item.setSummary(summary);

            list.add(item);
        }

        long total = response.getHits().getTotalHits().value;
        return ResponseService.builder().data("data", list)
                .data("total", total)
                .data("pages", total / PAGE_SIZE + 1).build();
    }
}
