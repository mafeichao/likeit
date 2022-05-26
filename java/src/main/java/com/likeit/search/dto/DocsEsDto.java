package com.likeit.search.dto;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DocsEsDto extends BaseEsDto {
    private Long uid;
    private String url;
    private String title;
    private String content;
    private String html_text;
    private String tags;
    private String summary;
    private String query;
    private String src;
    private String add_time;

    private Long es_time;

    private String id;

    public static DocsEsDto builder() {
        return new DocsEsDto();
    }

    public DocsEsDto uid(Long uid) {
        this.uid = uid;
        return this;
    }

    public DocsEsDto url(String url) {
        this.url = url;
        return this;
    }

    public DocsEsDto title(String title) {
        this.title = title;
        return this;
    }

    public DocsEsDto content(String content) {
        this.content = content;
        return this;
    }

    public DocsEsDto htmlText(String text) {
        this.html_text = text;
        return this;
    }

    public DocsEsDto tags(String tags) {
        this.tags = tags;
        return this;
    }

    public DocsEsDto summary(String summary) {
        this.summary = summary;
        return this;
    }

    public DocsEsDto query(String query) {
        this.query = query;
        return this;
    }

    public DocsEsDto source(String source) {
        this.src = source;
        return this;
    }

    public DocsEsDto addTime(String add_time) {
        this.add_time = add_time;
        return this;
    }

    @Override
    public Map<String, Object> build() {
        if(url == null || uid == null) {
            log.error("url or uid not setting, return empty data");
            return new HashMap<>();
        }

        id = DigestUtils.md5Hex(url + uid);
        es_time = System.currentTimeMillis();

        return mapping(log);
    }
}
