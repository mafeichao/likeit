package com.likeit.search.utils;

import cn.edu.hfut.dmic.webcollector.conf.Configuration;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.Requester;
import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;
import com.likeit.search.service.CrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author mafeichao
 */
@Slf4j
public class Tools {
    private static List<String> agentList = new ArrayList<>();
    static {
        agentList.add("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
        agentList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36");
        agentList.add("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.0 Safari/537.36");
        agentList.add("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2226.0 Safari/537.36");
        agentList.add("Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko");
        agentList.add("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/532.2 (KHTML, like Gecko) ChromePlus/4.0.222.3 Chrome/4.0.222.3 Safari/532.2");
        agentList.add("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.28.3 (KHTML, like Gecko) Version/3.2.3 ChromePlus/4.0.222.3 Chrome/4.0.222.3 Safari/525.28.3");
        agentList.add("Opera/9.80 (X11; Linux i686; Ubuntu/14.10) Presto/2.12.388 Version/12.16");
        agentList.add("Opera/9.80 (Windows NT 6.0) Presto/2.12.388 Version/12.14");
        agentList.add("Mozilla/5.0 (Windows NT 6.0; rv:2.0) Gecko/20100101 Firefox/4.0 Opera 12.14");
        agentList.add("Opera/12.80 (Windows NT 5.1; U; en) Presto/2.10.289 Version/12.02");
        agentList.add("Opera/9.80 (Windows NT 6.1; U; es-ES) Presto/2.9.181 Version/12.00");
        agentList.add("Opera/9.80 (Windows NT 5.1; U; zh-sg) Presto/2.9.181 Version/12.00");
        agentList.add("Opera/12.0(Windows NT 5.2;U;en)Presto/22.9.168 Version/12.00");
        agentList.add("Opera/12.0(Windows NT 5.1;U;en)Presto/22.9.168 Version/12.00");
        agentList.add("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1");
        agentList.add("Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0");
        agentList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10; rv:33.0) Gecko/20100101 Firefox/33.0");
        agentList.add("Mozilla/5.0 (X11; Linux i586; rv:31.0) Gecko/20100101 Firefox/31.0");
        agentList.add("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20130401 Firefox/31.0");
        agentList.add("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0");
        agentList.add("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.13 Safari/537.36");
        agentList.add("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.25 Safari/537.36 Core/1.70.3756.400 QQBrowser/10.5.4043.400");
    }

    public static String randomUserAgent() {
        Random rand = new Random();
        int idx = rand.nextInt(agentList.size());
        return agentList.get(idx);
    }

    public static Map<String, String> subMapString(Map<String, Object> data, int max) {
        Map<String, String> result = new HashMap<>();
        for(Map.Entry<String, Object> ent : data.entrySet()) {
            String value = ent.getValue().toString();
            int m = value.length() > max ? max : value.length();
            String str = ent.getValue() == null ? "" : value.substring(0, m);
            result.put(ent.getKey(), str);
        }
        return result;
    }

    public static Date now() {
        Date dt = new Date();
        return dt;
    }
    public static Date str2Date(String str, String fmt) {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        Date dt = null;
        try {
            dt = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }

    public static Date str2Date(String str) {
        return str2Date(str, "yyyy-MM-dd HH:mm:ss");
    }

    public static String date2Str(Date dt, String fmt) {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        String str = sdf.format(dt);
        return str;
    }

    public static String date2Str(Date dt) {
        return date2Str(dt, "yyyy-MM-dd HH:mm:ss");
    }

    public static Date addNdays(Date dt, int n) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(dt);
        calendar.add(Calendar.DATE, n);
        return calendar.getTime();
    }

    public static Long extractSearchCount(String txt) {
        List<Long> nums = new ArrayList<>();
        StringBuilder res = new StringBuilder();
        for(int i = 0;i < txt.length();++i) {
            char ch = txt.charAt(i);
            if(Character.isDigit(ch)) {
                res.append(ch);
            } else {
                if(ch != ',' && ch != ' ') {
                    if(res.length() > 0) {
                        nums.add(Long.valueOf(res.toString()));
                    }
                    res.setLength(0);
                }
            }
        }

        int len = nums.size();
        if(len > 0) {
            return nums.get(len - 1);
        }
        return 0l;
    }

    public static String extractBaiduUrl(String url) {
        Page data = CrawlerService.getPageByUrl(url);
        if(data == null) {
            log.error("extract baidu url failed:{}", url);
            return null;
        }

        Document doc = data.doc();
        if(doc != null && doc.text().length() > 0) {
            //not only contains jquery script
            Elements links = doc.head().select("link[rel='canonical']");
            if(links != null) {
                links = links.select("link[href]");
                if(links != null) {
                    return links.first().attr("href");
                }
            }
        }

        String html = data.html();
        int idx1 = html.indexOf("{");
        int idx2 = html.lastIndexOf("}");
        if(idx1 < 0 || idx2 < 0) {
            log.error("extract baidu url failed:{}, idx1:{}, idx2:{}", url, idx1, idx2);
            return null;
        }

        String jsonStr = html.substring(idx1, idx2 + 1);

        String anchor = "window.location.replace(\"";
        idx1 = jsonStr.indexOf(anchor);
        idx2 = jsonStr.indexOf("\")", idx1);
        if(idx1 < 0 || idx2 < 0) {
            log.error("extract baidu url failed:{}, idx1:{}, idx2:{}", url, idx1, idx2);
            return null;
        }

        String nurl = jsonStr.substring(idx1 + anchor.length(), idx2);
        return nurl;
    }

    public static void main(String[] args) {
        String str = "2022-05-12 17:05:05";
        Date dt = str2Date(str);
        Date dtp1d = addNdays(dt, 1);
        System.out.println("dtp1d:" + date2Str(dtp1d));

        Requester requester = new OkHttpRequester();
        Configuration.getDefault().setDefaultUserAgent(Tools.randomUserAgent());

        try {
            Page page = requester.getResponse("https://www.zhangfangzhou.cn/centos7-devtoolset8-gcc.html");
            System.out.println(page.url());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String md5 = DigestUtils.md5Hex("https://blog.csdn.net/t_xuanfeng123/article/details/107728016");
        System.out.println("md5:" + md5);

        System.out.println("total:" + extractSearchCount("百度为您找到相关结果约100,000,000个"));
        System.out.println("total:" + extractSearchCount("46,200,000 条结果"));
        System.out.println("total:" + extractSearchCount("11 - 20 条结果(共 46,200,000 条)"));
    }
}
