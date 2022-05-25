package com.likeit.search.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RestResponse {
    private Map<String, Object> data = new ConcurrentHashMap<>();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RestResponse response = new RestResponse();

        public Builder data(String key, Object value) {
            response.data.put(key, value);
            return this;
        }

        public Map<String, Object> build() {
            return response.data;
        }
    }
}
