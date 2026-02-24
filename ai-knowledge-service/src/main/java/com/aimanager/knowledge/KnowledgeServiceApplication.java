package com.aimanager.knowledge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 知识库服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.aimanager.knowledge", "com.aimanager.common"})
@MapperScan("com.aimanager.knowledge.mapper")
public class KnowledgeServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeServiceApplication.class, args);
    }
}

