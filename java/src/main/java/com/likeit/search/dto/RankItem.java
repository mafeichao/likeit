package com.likeit.search.dto;

import lombok.Data;

@Data
public class RankItem {
    private String title;
    private String summary;
    private String url;
    private String addTime;

    public static RankItem builder() {
        return new RankItem();
    }

    public RankItem title(String title) {
        this.title = title;
        return this;
    }

    public RankItem summary(String summary) {
        this.summary = summary;
        return this;
    }

    public RankItem url(String url) {
        this.url = url;
        return this;
    }

    public RankItem addTime(String addTime) {
        this.addTime = addTime;
        return this;
    }

    public RankItem build() {
        return this;
    }
}
