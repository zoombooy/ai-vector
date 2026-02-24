package com.aimanager.model.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * RestTemplate 配置
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // 确保使用 Jackson 进行 JSON 序列化
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        messageConverters.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
        messageConverters.add(new MappingJackson2HttpMessageConverter());
        
        return restTemplate;
    }
}

