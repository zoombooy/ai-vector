package com.aimanager.vector.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus 向量数据库配置
 */
@Slf4j
@Configuration
public class MilvusConfig {
    
    @Value("${milvus.host:localhost}")
    private String host;
    
    @Value("${milvus.port:19530}")
    private Integer port;
    
    @Bean
    public MilvusServiceClient milvusClient() {
        log.info("初始化 Milvus 客户端: {}:{}", host, port);
        
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port)
                .build();
        
        MilvusServiceClient client = new MilvusServiceClient(connectParam);
        
        log.info("Milvus 客户端初始化成功");
        return client;
    }
}

