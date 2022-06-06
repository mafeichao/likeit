package com.likeit.search.controller;

import com.alibaba.fastjson.JSON;
import com.likeit.search.dao.entity.likeit.UserUrlsEntity;
import com.likeit.search.dao.repository.likeit.UserUrlsRepository;
import com.likeit.search.dto.DocsEsDto;
import com.likeit.search.dto.HtmlEsDto;
import com.likeit.search.pa.PageAttribute;
import com.likeit.search.pa.PageInfo;
import com.likeit.search.service.EsService;
import com.likeit.search.service.ResponseService;
import com.likeit.search.utils.Consts;
import com.likeit.search.utils.Tools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author mafeichao
 */
@Slf4j
@RestController
@RequestMapping("/indexer")
public class IndexController {
    @Resource
    private UserUrlsRepository repository;

    @Resource
    private EsService esService;

    private String selectHtml(String url) {
        String html = null;

        List<String> htmls = esService.strEQStr(Consts.HTML_INDEX, "url", url, "html");
        for(String nHtml : htmls) {
            if(html == null) {
                html = nHtml;
            } else {
                if (html.length() < nHtml.length()) {
                    html = nHtml;
                }
            }
        }

        return html;
    }

    private boolean writeDocIndex(UserUrlsEntity data, PageInfo page) {
        DocsEsDto dto = DocsEsDto.builder().uid(data.getUid())
                .url(data.getUrl())
                .title(page == null || page.title == null ? "" : page.title)
                .content(page == null || page.content == null ? "" : page.content)
                .htmlText(page == null || page.doc == null ? "" : page.doc.text())
                .tags(data.getTags())
                .summary(data.getSummary())
                .source(data.getSource())
                .query(data.getQuery())
                .addTime(Tools.date2Str(data.getAdd_time()));

        return esService.indexData(Consts.DOCS_INDEX, dto.build());
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean writeHtmlDb(UserUrlsEntity entity) {
        try {
            UserUrlsEntity oldEntity = repository.getByUidUrl(entity.getUid(), entity.getUrl_sign());
            if(oldEntity != null) {
                log.info("oldEntity before:" + JSON.toJSONString(oldEntity));
                oldEntity.mergeAttr(entity);
                log.info("oldEntity after:" + JSON.toJSONString(oldEntity));
                repository.updateAttrs(oldEntity);
                entity.setId(oldEntity.getId());
            } else {
                repository.insert(entity);
            }
        } catch (Exception e) {
            log.error("write html db error:{},{},{}", JSON.toJSONString(entity), e.getMessage(), e.getStackTrace());
            return false;
        }
        return true;
    }

    @GetMapping("/index_url.json")
    public Object indexByUrl(@RequestParam Long uid, @RequestParam String url,
                             @RequestParam(required = false, defaultValue = "baidu") String source,
                             @RequestParam(required = false, defaultValue = "") String query,
                             @RequestParam(required = false, defaultValue = "") String tags,
                             @RequestParam(required = false, defaultValue = "") String summary) {
        //save to mysql
        Date now = Tools.now();
        UserUrlsEntity entity = new UserUrlsEntity();
        entity.setUid(uid);
        entity.setAdd_time(now);
        entity.setSource(source);
        entity.setQuery(query);
        entity.setUrl(url);
        entity.setTags(tags);
        entity.setSummary(summary);

        //get html and extract page info
        PageInfo info = PageAttribute.extract(url);
        if(info == null || !info.hasInfo()) {
            String html = selectHtml(url);

            if(html != null) {
                PageInfo anotherInfo = PageAttribute.extract(html, url);

                if(anotherInfo != null && anotherInfo.isBetter(info)) {
                    info = anotherInfo;
                }
            }
        }

        if(info == null) {
            return "下载分析url失败，请重试";
        }

        //save url info to db
        if(!writeHtmlDb(entity)) {
            log.error("fail to save url info to db:{}", JSON.toJSONString(entity));
        }

        //save html to es
        HtmlEsDto htmlDto = HtmlEsDto.builder().url(url)
                .html(info.doc.html())
                .dbId(entity.getId());
        boolean ret = esService.indexData(Consts.HTML_INDEX, htmlDto.build());
        if(!ret) {
            return "保存url失败，请重试";
        } else {
            repository.updateFlag(entity.getId(), 1);
        }

        //save docs to es
        ret = writeDocIndex(entity, info);
        if(!ret) {
            return "保存url失败，请重试！";
        }

        return "收藏成功！";
    }

    @GetMapping("/index_all.json")
    public Object indexAll() {
        int succ = 0;
        int fail = 0;
        int start = 0;
        int size = 100;
        List<UserUrlsEntity> data;
        while(true) {
            data = repository.getF1Urls(start, size);
            if(data == null || data.size() == 0) {
                break;
            }

            log.info("getAll start:{}, size:{}, real size:{}", start, size, data.size());
            for(UserUrlsEntity d : data) {
                String html = selectHtml(d.getUrl());
                if(html != null) {
                    PageInfo info = PageAttribute.extract(html, d.getUrl());
                   boolean ret = writeDocIndex(d, info);
                   if(ret) {
                       ++succ;
                   } else {
                       ++fail;
                   }
                } else {
                    ++fail;
                }
            }
            start += data.size();
        }
        log.info("indexAll finished:{}", start + (data == null ? 0 : data.size()));

        return ResponseService.builder().data("succ", succ)
                .data("fail", fail)
                .data("total", succ + fail).build();
    }
}
