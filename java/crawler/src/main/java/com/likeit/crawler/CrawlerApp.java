package com.likeit.crawler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author mafeichao
 */
@Slf4j
@SpringBootApplication
public class CrawlerApp {
    public static void main(String[] args) {
        SpringApplication.run(CrawlerApp.class, args);
    }
}
