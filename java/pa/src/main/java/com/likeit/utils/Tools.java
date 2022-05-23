package com.likeit.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author mafeichao
 */
public class Tools {
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

    public static void main(String[] args) {
        String str = "2022-05-12 17:05:05";
        Date dt = str2Date(str);
        Date dtp1d = addNdays(dt, 1);
        System.out.println("dtp1d:" + date2Str(dtp1d));
    }
}
