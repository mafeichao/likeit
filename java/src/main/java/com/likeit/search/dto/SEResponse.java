package com.likeit.search.dto;

import lombok.Data;

import java.util.List;

@Data
public class SEResponse {
    int code;
    String msg;
    List<RankItem> list;
    private long total;
}
