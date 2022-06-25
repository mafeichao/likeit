package com.likeit.search.service;

import cn.edu.hfut.dmic.webcollector.conf.Configuration;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.Requester;
import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;
import com.likeit.search.dto.RankItem;
import com.likeit.search.dto.SEResponse;
import com.likeit.search.utils.Tools;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

    static public SEResponse baiduSearch(String query, int page) {
        String url = String.format("https://www.baidu.com/s?wd=%s&pn=%d", query, (page - 1) * 10);
        Page data = CrawlerService.getPageByUrl(url);

        SEResponse response = new SEResponse();
        if(data == null) {
            log.info("baidu search failed:{}", url);
            response.setCode(404);
            response.setMsg("搜索百度失败");
        } else {
            Document doc = data.doc();
            Elements list = doc.select(".result.c-container.xpath-log.new-pmd");
            if(list == null) {
                log.error("baidu list null:{}", url);
                response.setCode(404);
                response.setMsg("百度搜索结果为空1");
            } else {
                Element total = doc.selectFirst(".hint_PIwZX.c_font_2AD7M");
                if(total == null) {
                    log.error("baidu total null:{}, html:{}", url, doc.html());
                    response.setCode(404);
                    response.setMsg("百度搜索结果为空2");
                    return response;
                }

                Long totalNum = Tools.extractSearchCount(total.text());
                //log.info("total:" + total.text() + ",num:" + totalNum);
                response.setTotal(totalNum);

                List<RankItem> listDoc = new ArrayList<>();
                for(Element ele : list) {
                    Element title = ele.selectFirst("a[href]");
                    //log.info("title:" + title.html());
                    String titleStr = title.html().replace("<em>", "<font color='red'>")
                            .replace("</em>", "</font>");

                    Element summary = ele.selectFirst(".content-right_8Zs40");
                    //log.info("summary:" + summary.html());
                    String summaryStr = summary.html().replace("<em>", "<font color='red'>")
                            .replace("</em>", "</font>");

                    String _url = title.attr("href");
                    //log.info("url:" + _url);

                    listDoc.add(RankItem.builder().url(_url)
                            .title(titleStr)
                            .summary(summaryStr)
                            .addTime("null"));
                }
                response.setList(listDoc);
                response.setMsg("success");
                response.setCode(200);
            }
        }
        return response;
    }

    static public SEResponse bingSearch(String query, int page) {
        String url = String.format("https://cn.bing.com/search?q=%s&first=%d", query, (page - 1) * 10 + 1);
        Page data = CrawlerService.getPageByUrl(url);

        SEResponse response = new SEResponse();
        if(data == null) {
            log.info("bing search failed:{}", url);
            response.setCode(404);
            response.setMsg("搜索Bing失败");
        } else {
            Document doc = data.doc();
            Elements list = doc.select(".b_algo");
            if(list == null) {
                log.error("Bing list null:{}", url);
                response.setCode(404);
                response.setMsg("Bing搜索结果为空1");
            } else {
                Element total = doc.selectFirst(".sb_count");
                if(total == null) {
                    log.error("Bing total null:{}, html:{}", url, doc.html());
                    response.setCode(404);
                    response.setMsg("Bing搜索结果为空2");
                    return response;
                }

                Long totalNum = Tools.extractSearchCount(total.text());
                //log.info("total:" + total.text() + ",num:" + totalNum);
                response.setTotal(totalNum);

                List<RankItem> listDoc = new ArrayList<>();
                for(Element ele : list) {
                    Element title = ele.select("h2").select("a[href]").first();
                    //log.info("title:" + title.html());
                    String titleStr = title.html().replace("<strong>", "<font color='red'>")
                            .replace("</strong>", "</font>");

                    Element summary = ele.selectFirst(".b_caption");
                    if(summary == null) {
                        log.info("bing naughty style, skip1");
                        continue;
                    }

                    summary = summary.selectFirst("p");
                    if(summary == null) {
                        log.info("bing naughty style, skip2");
                        continue;
                    }

                    //log.info("summary:" + summary.html());
                    String summaryStr = summary.html().replace("<strong>", "<font color='red'>")
                            .replace("</strong>", "</font>");

                    String _url = title.attr("href");
                    //log.info("url:" + _url);

                    listDoc.add(RankItem.builder().url(_url)
                            .title(titleStr)
                            .summary(summaryStr)
                            .addTime("null"));
                }
                response.setList(listDoc);
                response.setMsg("success");
                response.setCode(200);
            }
        }
        return response;
    }

    static public SEResponse sogouSearch(String query, int page) {
        String url = String.format("https://www.sogou.com/web?query=%s&page=%d", query, page);
        Page data = CrawlerService.getPageByUrl(url);

        SEResponse response = new SEResponse();
        if(data == null) {
            log.info("sogou search failed:{}", url);
            response.setCode(404);
            response.setMsg("搜索Sogou失败");
        } else {
            Document doc = data.doc();
            Elements list = doc.select(".vrwrap");
            if(list == null) {
                log.error("Sogou list null:{}", url);
                response.setCode(404);
                response.setMsg("Sogou搜索结果为空1");
            } else {
                Element total = doc.selectFirst(".num-tips");
                if(total == null) {
                    log.error("Sogou total null:{}, html:{}", url, doc.html());
                    response.setCode(404);
                    response.setMsg("Sogou搜索结果为空2");
                    return response;
                }

                Long totalNum = Tools.extractSearchCount(total.text());
                //log.info("total:" + total.text() + ",num:" + totalNum);
                response.setTotal(totalNum);

                List<RankItem> listDoc = new ArrayList<>();
                for(Element ele : list) {
                    Element title = ele.select("h3").select("a[href]").first();
                    //log.info("title:" + title.html());
                    String titleStr = title.html().replace("<em>", "<font color='red'>")
                            .replace("</em>", "</font>");

                    Element summary = ele.selectFirst(".space-txt");
                    if(summary == null) {
                        summary = ele.selectFirst(".step-list");
                        if(summary == null) {
                            log.info("sogou naughty style, skip1");
                            continue;
                        }
                    }

                    //log.info("summary:" + summary.html());
                    String summaryStr = summary.html().replace("<strong>", "<font color='red'>")
                            .replace("</strong>", "</font>");

                    String _url = title.attr("href");
                    //log.info("url:" + _url);

                    listDoc.add(RankItem.builder().url(_url)
                            .title(titleStr)
                            .summary(summaryStr)
                            .addTime("null"));
                }
                response.setList(listDoc);
                response.setMsg("success");
                response.setCode(200);
            }
        }
        return response;
    }

    static public SEResponse haosouSearch(String query, int page) {
        String url = String.format("https://www.so.com/s?q=%s&pn=%d", query, page);
        Page data = CrawlerService.getPageByUrl(url);

        SEResponse response = new SEResponse();
        if(data == null) {
            log.info("360 search failed:{}", url);
            response.setCode(404);
            response.setMsg("搜索360失败");
        } else {
            Document doc = data.doc();
            Elements list = doc.select(".res-list");
            if(list == null) {
                log.error("360 list null:{}", url);
                response.setCode(404);
                response.setMsg("360搜索结果为空1");
            } else {
                Element total = doc.selectFirst(".num");
                if(total == null) {
                    log.error("360 total null:{}, html:{}", url, doc.html());
                    response.setCode(404);
                    response.setMsg("360搜索结果为空2");
                    return response;
                }

                Long totalNum = Tools.extractSearchCount(total.text());
                //log.info("total:" + total.text() + ",num:" + totalNum);
                response.setTotal(totalNum);

                List<RankItem> listDoc = new ArrayList<>();
                for(Element ele : list) {
                    Element title = ele.select("h3").select("a[href]").first();
                    //log.info("title:" + title.html());
                    String titleStr = title.html().replace("<em>", "<font color='red'>")
                            .replace("</em>", "</font>");

                    Element summary = ele.selectFirst(".res-rich");
                    if(summary == null) {
                        log.info("360 naughty style, skip1");
                        continue;
                    }

                    //log.info("summary:" + summary.html());
                    String summaryStr = summary.html().replace("<strong>", "<font color='red'>")
                            .replace("</strong>", "</font>");

                    String _url = title.attr("href");
                    //log.info("url:" + _url);

                    listDoc.add(RankItem.builder().url(_url)
                            .title(titleStr)
                            .summary(summaryStr)
                            .addTime("null"));
                }
                response.setList(listDoc);
                response.setMsg("success");
                response.setCode(200);
            }
        }
        return response;
    }

    static public SEResponse googleSearch(String query, int page) {
        SEResponse response = null;
        return response;
    }
}
