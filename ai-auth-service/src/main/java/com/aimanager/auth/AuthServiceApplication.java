package com.aimanager.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 认证授权服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.aimanager.auth", "com.aimanager.common"})
@MapperScan("com.aimanager.auth.mapper")
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

