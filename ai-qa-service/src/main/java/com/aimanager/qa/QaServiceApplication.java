package com.aimanager.qa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI问答服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.aimanager.qa", "com.aimanager.common"})
@MapperScan("com.aimanager.qa.mapper")
public class QaServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(QaServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("AI问答服务启动成功！");
        System.out.println("端口: 8086");
        System.out.println("========================================");
    }
}

