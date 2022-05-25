package com.likeit.search.dto;

import lombok.Data;

@Data
public class RankItem {
    private String rank;
    private String title;
    private String summary;
    private String url;
    private String addTime;
}
