package com.likeit.search.pa;

import cn.edu.hfut.dmic.contentextractor.ContentExtractor;
import cn.edu.hfut.dmic.contentextractor.News;
import cn.edu.hfut.dmic.webcollector.conf.Configuration;
import com.likeit.search.utils.Tools;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mafeichao
 */
@Slf4j
public class PageAttribute {
    private static void fillPageInfo(PageInfo info, News news) {
        info.url = news.getUrl();
        info.title = news.getTitle();
        info.content = news.getContent();
        info.time = news.getTime();
        info.doc = news.getDoc();
    }

    public static PageInfo extract(String html, String url) {
        try {
            PageInfo info = new PageInfo();
            News news = ContentExtractor.getNewsByHtml(html, url);
            fillPageInfo(info, news);
            return info;
        } catch (Exception e) {
            log.warn("extract failed:{},{},{}", url, e.getMessage() , e.getStackTrace());
            return null;
        }
    }

    public static PageInfo extract(String url) {
        try {
            String ua = Tools.randomUserAgent();
            Configuration.getDefault().setDefaultUserAgent(ua);

            PageInfo info = new PageInfo();
            News news = ContentExtractor.getNewsByUrl(url);
            fillPageInfo(info, news);
            return info;
        } catch (Exception e) {
            log.warn("extract failed:{},{},{}", url, e.getMessage() , e.getStackTrace());
            return null;
        }
    }

    public static void main(String[] args) {
        String url = "http://m.blog.csdn.net/qq_40027052/article/details/78733365";
        PageInfo info = PageAttribute.extract(url);
        log.info("info:\n" + info);
    }
}
