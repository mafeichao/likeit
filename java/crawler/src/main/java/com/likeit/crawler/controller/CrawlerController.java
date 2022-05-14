package com.likeit.crawler.controller;

import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.Requester;
import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;
import com.alibaba.fastjson.JSON;
import com.likeit.crawler.dao.entity.likeit.UserUrlsEntity;
import com.likeit.crawler.dao.repository.likeit.UserUrlsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("/crawler")
public class CrawlerController {
    private static final String HTML_INDEX = "likeit_htmls";

    @Resource
    private UserUrlsRepository repository;

    @Resource
    private RestHighLevelClient esClient;

    @GetMapping("/get_all.json")
    public Object getAll() {
        Requester requester = new OkHttpRequester();

        int succ = 0;
        int fail = 0;
        int start = 0;
        int size = 100;
        while(true) {
            List<UserUrlsEntity> data = repository.getUrls(start, size);
            if(data == null || data.size() == 0) {
                break;
            }

            log.info("getAll start:{}, size:{}, real size:{}", start, size, data.size());
            for(UserUrlsEntity d : data) {
                try {
                    Page page = requester.getResponse(d.getUrl());
                    String text = page.doc().text();
                    if(text != null) {
                        int max = Math.min(text.length(), 30);
                        text = text.substring(0, max);
                    }
                    log.info("get url succeed:{},{}", d.getUrl(), text);

                    long es_time = System.currentTimeMillis();
                    //keep multi versions
                    String id = DigestUtils.md5Hex(d.getUrl() + es_time);
                    Map<String, Object> source = new HashMap<>();
                    source.put("url", d.getUrl());
                    source.put("html", page.html());
                    source.put("db_id", d.getId());
                    source.put("es_time", es_time);

                    IndexRequest request = new IndexRequest();
                    request.index(HTML_INDEX).id(id).source(source);

                    try {
                        IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);
                        repository.updateFlag(d.getId(), 1);
                        ++succ;

                        source.remove("html");
                        log.info("index succeed:{},{}", JSON.toJSONString(source), response.toString());
                    } catch (Exception e) {
                        log.warn("index failed:{},{},{}", d.getUrl(), e.getMessage(), e.getStackTrace());
                    }
                } catch (Exception e) {
                    ++fail;
                    repository.updateFlag(d.getId(), 2);
                    log.warn("get url failed:{},{},{}", d.getUrl(), e.getMessage(), e.getStackTrace());
                }
            }
            start += data.size();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("succ", succ);
        result.put("fail", fail);
        result.put("total", succ + fail);
        return result;
    }
}
