package com.likeit.index.controller;

import com.alibaba.fastjson.JSON;
import com.likeit.index.dao.likeit.UserUrlsEntity;
import com.likeit.index.dao.likeit.UserUrlsRepository;
import com.likeit.pa.PageAttribute;
import com.likeit.pa.PageInfo;
import com.likeit.utils.Tools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mafeichao
 */
@Slf4j
@RestController
@RequestMapping("/indexer")
public class IndexController {
    private static final String HTML_INDEX = "likeit_htmls";
    private static final String DOC_INDEX = "likeit_docs";

    @Resource
    private UserUrlsRepository repository;

    @Resource
    private RestHighLevelClient esClient;

    private String selectHtml(String url) {
        String html = null;

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.termQuery("url", url));

        SearchRequest request = new SearchRequest(HTML_INDEX);
        request.source(builder);
        log.info("search dsl:{}", builder.toString());

        try {
            SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            if(hits != null && hits.length > 0) {
                for(SearchHit hit : hits) {
                    String nHtml = hit.getSourceAsMap().get("html").toString();

                    if(html == null) {
                        html = nHtml;
                    } else {
                        if (html.length() < nHtml.length()) {
                            html = nHtml;
                        }
                    }
                }
                log.info("search succeed:{}, find:{} for url:{}", HTML_INDEX, hits.length, url);
            }
        } catch (Exception e) {
            log.error("search failed:{},{},{},{}", HTML_INDEX, url, e.getMessage(), e.getStackTrace());
        }

        return html;
    }

    private boolean writeDocIndex(UserUrlsEntity data, PageInfo page) {
        Map<String, Object> result = new HashMap<>();

        result.put("add_time", data.getAddTime());
        result.put("content", page == null || page.content == null ? "" : page.content);
        result.put("es_time", System.currentTimeMillis());
        result.put("html_text", page == null || page.doc == null ? "" : page.doc.text());
        result.put("query", data.getQuery());
        result.put("src", data.getSource());
        result.put("summary", data.getSummary());
        result.put("tags", data.getTags());
        result.put("title", page == null || page.title == null ? "" : page.title);
        result.put("uid", data.getUid());
        result.put("url", data.getUrl());

        String id = DigestUtils.md5Hex(data.getUrl() + data.getUid());
        IndexRequest request = new IndexRequest();
        request.index(DOC_INDEX).id(id).source(result).timeout(TimeValue.timeValueSeconds(1));
        try {
            IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);
            log.info("index succeed:{},{}", JSON.toJSONString(Tools.subMapString(result, 30)), response.toString());
            return true;
        } catch (Exception e) {
            log.error("index failed:{},{},{}", JSON.toJSONString(Tools.subMapString(result, 30)), e.getMessage(), e.getStackTrace());
            return false;
        }
    }

    @GetMapping("/index_url.json")
    public Object indexByUrl(@RequestParam String url) {
        List<UserUrlsEntity> data = repository.getByUrl(url);
        if(data != null && data.size() > 0) {
            for(UserUrlsEntity d : data) {
                String html = selectHtml(d.getUrl());
                if(html != null) {
                    PageInfo info = PageAttribute.extract(html, url);
                    writeDocIndex(d, info);
                } else {
                    return String.format("%s url has no html", url);
                }
            }
            return String.format("%s url processed:%s", data.size(), url);
        } else {
            return "No url found in Mysql:" + url;
        }
    }

    @GetMapping("/index_all.json")
    public Object indexAll() {
        int succ = 0;
        int fail = 0;
        int start = 0;
        int size = 100;
        List<UserUrlsEntity> data;
        while(true) {
            data = repository.getUrls(start, size);
            if(data == null || data.size() == 0) {
                break;
            }

            log.info("getAll start:{}, size:{}, real size:{}", start, size, data.size());
            for(UserUrlsEntity d : data) {
                String html = selectHtml(d.getUrl());
                if(html != null) {
                    PageInfo info = PageAttribute.extract(html, d.getUrl());
                   boolean ret = writeDocIndex(d, info);
                   if(ret) {
                       ++succ;
                   } else {
                       ++fail;
                   }
                } else {
                    ++fail;
                }
            }
            start += data.size();
        }
        log.info("indexAll finished:{}", (start + data.size()));

        Map<String, Object> result = new HashMap<>();
        result.put("succ", succ);
        result.put("fail", fail);
        result.put("total", succ + fail);
        return result;
    }
}
