package com.likeit.search;

import com.likeit.search.service.EsService;
import com.likeit.search.utils.Consts;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;


@RunWith(SpringRunner.class)

@SpringBootTest(classes = SearchApp.class)

public class TestApp {
    static class Father {

    }

    private static class Son extends Father {

    }

    static public Son fSon() {
        return new Son();
    }


    @Resource
    EsService esService;

    @Test
    public void testStrEQStr() {
        String url = "https://blog.csdn.net/qq_34322008/article/details/89954934";
        List<String> htmls = esService.strEQStr(Consts.HTML_INDEX, "url", url, "html");
        for(String nHtml : htmls) {
            int max = Math.min(30, nHtml.length());
            System.out.println("===html:" + nHtml.substring(0, max));
        }
    }

    public static void main(String[] args) {
        Father f1 = new Father();
        Father f2 = new Son();
        Father f3 = fSon();
    }
}