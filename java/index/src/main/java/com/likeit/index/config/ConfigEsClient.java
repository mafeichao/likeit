package com.likeit.index.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author mafeichao
 */
@Slf4j
@Configuration
public class ConfigEsClient {
    @Value("${elasticsearch.cluster.hosts}")
    private String hosts;
    @Value("${elasticsearch.cluster.ports}")
    private String ports;

    // 连接超时时间
    private static int connectTimeOut = 1000;
    // 连接超时时间
    private static int socketTimeOut = 30000;
    // 连接超时时间
    private static int socketTimeOutLong = 300000;
    // 获取连接的超时时间
    private static int connectionRequestTimeOut = 500;
    // 最大连接数
    private static int maxConnectNum = 300;
    // 最大路由连接数
    private static int maxConnectPerRoute = 300;

    @Bean(name="esClient")
    public RestHighLevelClient esClient() {
        String[] clusterHosts = hosts.split(",");
        String[] clusterPorts = ports.split(",");
        HttpHost[] httpHosts = new HttpHost[clusterHosts.length];
        for (int i = 0; i < clusterHosts.length; i++) {
            httpHosts[i] = new HttpHost(clusterHosts[i], Integer.valueOf(clusterPorts[i]));
        }
        RestClientBuilder builder = RestClient.builder(httpHosts);

        // 异步httpclient连接延时配置
        builder.setRequestConfigCallback(b -> {
            b.setConnectTimeout(connectTimeOut);
            b.setConnectionRequestTimeout(connectionRequestTimeOut);
            b.setSocketTimeout(socketTimeOut);
            return b;
        });

        // 异步httpclient连接数配置
        builder.setHttpClientConfigCallback(b -> {
            b.setMaxConnTotal(maxConnectNum);
            b.setMaxConnPerRoute(maxConnectPerRoute);
            return b;
        });

        return new RestHighLevelClient(builder);
    }
}
