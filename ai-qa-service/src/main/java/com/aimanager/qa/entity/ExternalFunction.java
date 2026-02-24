package com.aimanager.qa.entity;

import com.aimanager.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 外部函数定义实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_external_function")
public class ExternalFunction extends BaseEntity {
    
    /**
     * 函数名称（唯一标识）
     */
    private String functionName;
    
    /**
     * 显示名称
     */
    private String displayName;
    
    /**
     * 函数描述（告诉AI这个函数的用途）
     */
    private String description;
    
    /**
     * API接口地址
     */
    private String apiUrl;
    
    /**
     * HTTP方法（GET/POST/PUT/DELETE）
     */
    private String httpMethod;
    
    /**
     * 请求头（JSON格式）
     */
    private String headers;
    
    /**
     * 认证类型（NONE/BEARER/API_KEY/BASIC）
     */
    private String authType;
    
    /**
     * 认证配置（JSON格式）
     */
    private String authConfig;
    
    /**
     * 参数Schema（JSON Schema格式）
     */
    private String parametersSchema;
    
    /**
     * 响应映射配置（JSON格式）
     */
    private String responseMapping;
    
    /**
     * 超时时间（秒）
     */
    private Integer timeout;
    
    /**
     * 重试次数
     */
    private Integer retryTimes;
    
    /**
     * 状态（0-禁用 1-启用）
     */
    private Integer status;
    
    /**
     * 分类
     */
    private String category;
}

