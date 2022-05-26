package com.likeit.search.service;

import cn.edu.hfut.dmic.webcollector.conf.Configuration;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.Requester;
import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;
import com.likeit.search.utils.Tools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CrawlerService {
    static public Page getPageByUrl(String url) {
        Requester requester = new OkHttpRequester();
        String ua = Tools.randomUserAgent();
        Configuration.getDefault().setDefaultUserAgent(ua);

        Page page = null;
        try {
            page = requester.getResponse(url);
            String text = page.doc().text();
            if(text != null) {
                int max = Math.min(text.length(), 30);
                text = text.substring(0, max);
            }
            log.info("get url succeed:{},{},{}", url, text, ua);
        } catch (Exception e) {
            log.error("get url failed:{},{},{}", url, ua, e.getMessage(), e.getStackTrace());
        }
        return page;
    }
}
