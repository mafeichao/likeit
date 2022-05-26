package com.likeit.search.pa;

import org.jsoup.nodes.Document;

/**
 * @author mafeichao
 */
public class PageInfo {
    public String url;
    public String title;
    public String content;
    public String time;
    public Document doc;

    public boolean hasInfo() {
        return title != null && content != null;
    }

    public boolean isBetter(PageInfo another) {
        return doc.html().length() > another.doc.html().length();

    }
    @Override
    public String toString() {
        String result = ("URL:" + url) +
                "\nTITLE:" + title +
                "\nTIME:" + time +
                "\nCONTENT:" + content;
        return result;
    }
}
