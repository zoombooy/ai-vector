package com.aimanager.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI模型VO（返回给前端的数据）
 */
@Data
public class AiModelVO {
    
    /**
     * 模型ID
     */
    private Long id;
    
    /**
     * 模型名称
     */
    private String modelName;
    
    /**
     * 模型编码
     */
    private String modelCode;
    
    /**
     * 模型类型
     */
    private String modelType;
    
    /**
     * 模型版本
     */
    private String modelVersion;
    
    /**
     * 提供商
     */
    private String provider;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    // ========== 配置信息 ==========
    
    /**
     * 配置ID
     */
    private Long configId;
    
    /**
     * API地址
     */
    private String apiUrl;
    
    /**
     * API密钥（脱敏显示）
     */
    private String apiKey;
    
    /**
     * 最大Token数
     */
    private Integer maxTokens;
    
    /**
     * 温度参数
     */
    private BigDecimal temperature;
    
    /**
     * Top P参数
     */
    private BigDecimal topP;
    
    /**
     * 频率惩罚
     */
    private BigDecimal frequencyPenalty;
    
    /**
     * 存在惩罚
     */
    private BigDecimal presencePenalty;
    
    /**
     * 超时时间（秒）
     */
    private Integer timeout;
    
    /**
     * 重试次数
     */
    private Integer retryTimes;
    
    /**
     * 其他配置
     */
    private String configJson;
}

