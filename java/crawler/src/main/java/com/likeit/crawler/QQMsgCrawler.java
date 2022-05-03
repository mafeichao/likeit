package com.likeit.crawler;

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
        LOG.info("URL:" + page.url());
        LOG.info("DOC:" + page.doc().text());
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