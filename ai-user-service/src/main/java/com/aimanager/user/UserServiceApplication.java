package com.aimanager.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 用户组织管理服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.aimanager.user", "com.aimanager.common"})
@MapperScan("com.aimanager.user.mapper")
public class UserServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("用户组织管理服务启动成功！");
        System.out.println("端口: 8082");
        System.out.println("========================================");
    }
}

