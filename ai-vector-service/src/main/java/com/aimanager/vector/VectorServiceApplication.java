package com.aimanager.vector;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 向量处理服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.aimanager.vector", "com.aimanager.common"})
@MapperScan("com.aimanager.vector.mapper")
public class VectorServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(VectorServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("向量处理服务启动成功！");
        System.out.println("端口: 8085");
        System.out.println("========================================");
    }
}

