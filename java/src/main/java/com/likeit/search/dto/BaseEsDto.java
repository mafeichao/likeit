package com.likeit.search.dto;

import java.lang.reflect.Field;
import java.util.*;

abstract public class BaseEsDto {
    abstract public Map<String, Object> build();

    public Map<String, Object> mapping(Object... args) {
        Set<Object> noPuts = new HashSet<>();
        Collections.addAll(noPuts, args);

        Map<String, Object> map = new HashMap<>();

        Field[] fields = this.getClass().getDeclaredFields();
        for(Field field : fields) {
            field.setAccessible(true);
            try {
                Object obj = field.get(this);
                if(noPuts.contains(obj)) {
                    continue;
                }

                map.put(field.getName(), obj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return map;
    }
}
