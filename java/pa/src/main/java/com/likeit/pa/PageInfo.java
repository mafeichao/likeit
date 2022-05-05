package com.likeit.pa;

/**
 * @author mafeichao
 */
public class PageInfo {
    public String url;
    public String title;
    public String content;
    public String time;

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("URL:" + url);
        result.append("\nTITLE:" + title);
        result.append("\nTIME:" + time);
        result.append("\nCONTENT:" + content);
        return result.toString();
    }
}
