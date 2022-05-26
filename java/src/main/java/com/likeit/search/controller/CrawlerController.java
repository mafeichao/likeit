package com.likeit.search.controller;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.likeit.search.dao.entity.likeit.UserUrlsEntity;
import com.likeit.search.dao.repository.likeit.UserUrlsRepository;
import com.likeit.search.dto.HtmlEsDto;
import com.likeit.search.service.CrawlerService;
import com.likeit.search.service.EsService;
import com.likeit.search.service.ResponseService;
import com.likeit.search.utils.Consts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author mafeichao
 */
@Slf4j
@RestController
@RequestMapping("/crawler")
public class CrawlerController {
    @Resource
    private UserUrlsRepository repository;

    @Resource
    private EsService esService;

    private boolean getByData(UserUrlsEntity d) {
        Page page = CrawlerService.getPageByUrl(d.getUrl());
        if (page != null) {
            HtmlEsDto dto = HtmlEsDto.builder().url(d.getUrl())
                    .html(page.html())
                    .dbId(d.getId());

            boolean ret = esService.indexData(Consts.HTML_INDEX, dto.build());
            if(ret) {
                repository.updateFlag(d.getId(), 1);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @GetMapping("/get_all.json")
    public Object getAll() {
        int succ = 0;
        int fail = 0;
        int start = 0;
        int size = 100;
        while(true) {
            List<UserUrlsEntity> data = repository.getNF1Urls(start, size);
            if(data == null || data.size() == 0) {
                break;
            }

            log.info("getAll start:{}, size:{}, real size:{}", start, size, data.size());
            for(UserUrlsEntity d : data) {
                if(getByData(d)) {
                    ++succ;
                } else {
                    ++fail;
                }
            }
            start += data.size();
        }

        return ResponseService.builder().data("succ", succ)
                .data("fail", fail)
                .data("total", succ + fail).build();
    }
}
