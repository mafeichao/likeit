package com.likeit.search.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ResponseService {
    private Map<String, Object> data = new ConcurrentHashMap<>();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ResponseService response = new ResponseService();

        public Builder data(String key, Object value) {
            response.data.put(key, value);
            return this;
        }

        public Map<String, Object> build() {
            return response.data;
        }
    }
}
