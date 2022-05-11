package com.likeit.utils;

import java.util.HashMap;
import java.util.Map;

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
}
