package com.aimanager.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * AI模型DTO（包含模型和配置信息）
 */
@Data
public class AiModelDTO {
    
    /**
     * 模型ID（更新时需要）
     */
    private Long id;
    
    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    private String modelName;
    
    /**
     * 模型编码
     */
    @NotBlank(message = "模型编码不能为空")
    private String modelCode;
    
    /**
     * 模型类型
     */
    @NotBlank(message = "模型类型不能为空")
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
    @NotNull(message = "状态不能为空")
    private Integer status;
    
    /**
     * 描述
     */
    private String description;
    
    // ========== 配置信息 ==========
    
    /**
     * 配置ID（更新时需要）
     */
    private Long configId;
    
    /**
     * API地址
     */
    @NotBlank(message = "API地址不能为空")
    private String apiUrl;
    
    /**
     * API密钥
     */
    @NotBlank(message = "API密钥不能为空")
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
     * 其他配置（JSON格式）
     */
    private String configJson;
}

