package com.likeit.search.dto;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HtmlEsDto extends BaseEsDto {
    private String url;
    private String html;
    private Long db_id;

    private Long es_time;

    private String id;

    public static HtmlEsDto builder() {
        return new HtmlEsDto();
    }

    public HtmlEsDto url(String url) {
        this.url = url;
        return this;
    }

    public HtmlEsDto html(String html) {
        this.html = html;
        return this;
    }

    public HtmlEsDto dbId(Long dbId) {
        this.db_id = dbId;
        return this;
    }

    @Override
    public Map<String, Object> build() {
        //keep multi versions
        es_time = System.currentTimeMillis();

        if (url == null) {
            log.error("url not setting, return empty data");
            return new HashMap<>();
        }

        id = DigestUtils.md5Hex(url + es_time);

        return mapping(log);
    }
}
