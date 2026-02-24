package com.aimanager.model;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI模型管理服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.aimanager.model", "com.aimanager.common"})
@MapperScan("com.aimanager.model.mapper")
public class ModelServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ModelServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("AI模型管理服务启动成功！");
        System.out.println("端口: 8084");
        System.out.println("========================================");
    }
}

