package com.likeit.crawler.dao.entity.likeit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author mafeichao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUrlsEntity {
    private Long id;
    private Long uid;
    private Date addTime;
    private String source;
    private String query;
    private String url;
    private String tags;
    private String summary;
    private Date updateTime;
    private Integer flag;
}
