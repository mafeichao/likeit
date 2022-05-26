package com.likeit.search.controller;

import com.likeit.search.dao.entity.likeit.UserUrlsEntity;
import com.likeit.search.dao.repository.likeit.UserUrlsRepository;
import com.likeit.search.service.ResponseService;
import com.likeit.search.utils.Tools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mafeichao
 */
@Slf4j
@RestController
@RequestMapping("/importer")
public class ImportController {
    @Resource
    private UserUrlsRepository repository;

    /**
     * parse到一个date，表示如下含义
     * 1，代表一个老数据的结束，输出容器中的老数据
     * 2，代表一个新数据的开始，重置容器存放新数据
     * 3，当前时间比上一个时间小，需要递增dt，否则沿用dt
     */
    private String parseDate(String line, String tm, Date dt) {
        if (line.startsWith("我的手机") || line.startsWith("我的电脑")) {
            String[] fds = line.split("\\s+");
            String ctm = fds[fds.length - 1];
            if (tm != null && ctm.compareTo(tm.split("\\s+")[1]) < 0) {
                Date dt1 = Tools.addNdays(dt, 1);
                dt.setTime(dt1.getTime());
            }
            return Tools.date2Str(dt, "yyyy-MM-dd") + " " + ctm;
        }
        return null;
    }

    private void saveData(Long uid, List<String> data, String pTime,
                          AtomicInteger httpCount, AtomicInteger msgCount) {
        StringBuilder msg = new StringBuilder();
        for (String d : data) {
            if (d.startsWith("http")) {
                UserUrlsEntity entity = new UserUrlsEntity();
                entity.setUid(uid);
                entity.setAddTime(Tools.str2Date(pTime));
                entity.setSource("qqmsg");
                entity.setQuery("");
                entity.setUrl(d);
                entity.setTags("");
                entity.setSummary("");
                repository.insert(entity);
                log.info("http data:{},{}", entity.toString(), pTime);
                httpCount.incrementAndGet();
            } else {
                if (msg.length() > 0) {
                    msg.append("|");
                }
                msg.append(d);
            }
        }

        if (msg.length() > 0) {
            log.info("msg data:{}\t{}", msg.toString(), pTime);
            msgCount.incrementAndGet();
        }
    }

    @GetMapping("/qq_msg.json")
    public Object importQQMsg(@RequestParam("file") MultipartFile file,
                              @RequestParam("date") String sDate,
                              @RequestParam("uid") Long uid) {
        try {
            Date date = Tools.str2Date(sDate, "yyyy-MM-dd");
            String pTime = null;
            AtomicInteger msgCount = new AtomicInteger(0);
            AtomicInteger httpCount = new AtomicInteger(0);
            List<String> data = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }

                line = line.trim();
                if (line.equals("")) {
                    continue;
                }

                String cTime = parseDate(line, pTime, date);
                if (cTime != null) {
                    if (data.size() > 0) {
                        saveData(uid, data, pTime, httpCount, msgCount);
                        data.clear();
                    }
                    pTime = cTime;
                } else {
                    data.add(line);
                }
            }

            if(data.size() > 0) {
                saveData(uid, data, pTime, httpCount, msgCount);
            }

            return ResponseService.builder().data("code", 200)
                    .data("count", "msgCount:" + msgCount + ",httpCount:" + httpCount + ",totalCount:"
                            + (msgCount.get() + httpCount.get()))
                    .data("msg", "succeed, now date:" + Tools.date2Str(date, "yyyy-MM-dd")).build();
        } catch (IOException e) {
            return ResponseService.builder().data("code", 500)
                    .data("msg", e.getMessage())
                    .data("stack", e.getStackTrace()).build();
        }
    }
}
