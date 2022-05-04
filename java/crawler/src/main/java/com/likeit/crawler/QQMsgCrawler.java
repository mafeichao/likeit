package com.likeit.crawler;

import cn.edu.hfut.dmic.contentextractor.ContentExtractor;
import cn.edu.hfut.dmic.contentextractor.News;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class QQMsgCrawler extends BreadthCrawler {
    static Logger LOG = LoggerFactory.getLogger(QQMsgCrawler.class);

    public QQMsgCrawler(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);

        this.setThreads(10);
    }

    public void visit(Page page, CrawlDatums crawlDatums) {
        StringBuilder result = new StringBuilder();
        result.append("\nURL:" + page.url());
        String text = page.doc().text();
        result.append("\nDOC:" + text.substring(0, text.length() > 100 ? 100 : text.length()));

        try {
            News news = ContentExtractor.getNewsByHtml(page.html(), page.url());

            result.append("\nTitle:" + news.getTitle());
            text = news.getContent();
            result.append("\nContent:" + text.substring(0, text.length() > 100 ? 100 : text.length()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        LOG.info(result.toString());
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

            line = line.trim();
            if(line.startsWith("http")) {
                crawler.addSeed(line);
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
