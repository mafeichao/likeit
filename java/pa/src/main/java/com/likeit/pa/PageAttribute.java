package com.likeit.pa;

import cn.edu.hfut.dmic.contentextractor.ContentExtractor;
import cn.edu.hfut.dmic.contentextractor.News;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mafeichao
 */
public class PageAttribute {
    static Logger LOG = LoggerFactory.getLogger(PageAttribute.class);

    public static PageInfo extract(String html, String url) {
        try {
            PageInfo info = new PageInfo();
            News news = ContentExtractor.getNewsByHtml(html, url);
            info.url = news.getUrl();
            info.title = news.getTitle();
            info.content = news.getContent();
            info.time = news.getTime();
            return info;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.warn("extract failed:{},{},{}", url, e.getMessage() , e.getStackTrace());
            return null;
        }
    }

    public static PageInfo extract(String url) {
        try {
            PageInfo info = new PageInfo();
            News news = ContentExtractor.getNewsByUrl(url);
            info.url = news.getUrl();
            info.title = news.getTitle();
            info.content = news.getContent();
            info.time = news.getTime();
            return info;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.warn("extract failed:{},{},{}", url, e.getMessage() , e.getStackTrace());
            return null;
        }
    }

    public static void main(String[] args) {
        String url = "http://m.blog.csdn.net/qq_40027052/article/details/78733365";
        PageInfo info = PageAttribute.extract(url);
        LOG.info("info:\n" + info);
    }
}
