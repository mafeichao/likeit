package com.likeit.crawler;

import cn.edu.hfut.dmic.contentextractor.ContentExtractor;
import cn.edu.hfut.dmic.contentextractor.News;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class QQMsgCrawler extends BreadthCrawler {
    static String INDEX = "likeit_htmls";
    static Logger LOG = LoggerFactory.getLogger(QQMsgCrawler.class);
    static RestHighLevelClient esClient;

    public QQMsgCrawler(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);

        this.setThreads(10);

        esClient = new RestHighLevelClient(RestClient.builder(new HttpHost("82.157.160.66",9200)));
    }

    @Override
    public void visit(Page page, CrawlDatums crawlDatums) {
        String url = page.url();
        String src = page.meta("src");
        String add_query = page.meta("add_query");
        String add_time = page.meta("add_time");
        String text = page.doc().text();

        StringBuilder result = new StringBuilder();
        result.append("\nURL:" + url);
        result.append("\nDOC:" + text.substring(0, text.length() > 100 ? 100 : text.length()));

        try {
            News news = ContentExtractor.getNewsByHtml(page.html(), page.url());

            result.append("\nTitle:" + news.getTitle());
            text = news.getContent();
            result.append("\nContent:" + text.substring(0, text.length() > 100 ? 100 : text.length()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.append("\nAddTime:" + add_time + ",AddQuery:" + add_query);
        LOG.info(result.toString());

        String id = DigestUtils.md5Hex(url + src + add_query + add_time);
        Map<String, String> data = new HashMap<String, String>(5);
        data.put("url", url);
        data.put("src", src);
        data.put("query", add_query);
        data.put("add_time", add_time);
        data.put("html", page.html());

        IndexRequest request = new IndexRequest();
        request.index(INDEX).id(id).source(data).timeout(TimeValue.timeValueSeconds(1));
        try {
            IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);
            data.remove("html");
            LOG.info("index succeed:{},{}", JSON.toJSONString(data), response.toString());
        } catch (Exception e) {
            data.remove("html");
            LOG.error("index failed:{},{},{}", JSON.toJSONString(data), e.getMessage(), e.getStackTrace());
        }
    }

    public static void main(String[] args) throws Exception {
        String crawlPath = null;
        String seedPath = null;
        if(args.length < 2) {
            LOG.error("main crawlPath seedPath");
            System.exit(-1);
        } else {
            crawlPath = args[0];
            seedPath = args[1];
            LOG.info("crawlPath:" + crawlPath + ",seedPath:" + seedPath);
        }

        QQMsgCrawler crawler = new QQMsgCrawler(crawlPath, false);
        FileInputStream fstream = new FileInputStream(seedPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fstream));
        String line;
        int count = 0;
        while(true) {
            line = reader.readLine();
            if(line == null) {
                break;
            }

            String[] fds = line.trim().split("\t");
            if(line.startsWith("http")) {
                CrawlDatum data = new CrawlDatum(fds[0]);
                data.meta("add_time", fds[1]);
                data.meta("add_query", null);
                data.meta("src", "qq_msg");
                crawler.addSeed(data);
                ++count;
            } else {
                LOG.info("skip:" + line);
            }
        }
        LOG.info("total seed:" + count);

        crawler.start(1);
        LOG.info("finish");
    }
}
