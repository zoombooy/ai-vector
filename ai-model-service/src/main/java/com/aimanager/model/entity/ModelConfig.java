package com.aimanager.model.entity;

import com.aimanager.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 模型配置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_model_config")
public class ModelConfig extends BaseEntity {
    
    /**
     * 模型ID
     */
    private Long modelId;
    
    /**
     * API地址
     */
    private String apiUrl;
    
    /**
     * API密钥（加密）
     */
    private String apiKey;
    
    /**
     * 最大Token数
     */
    private Integer maxTokens;
    
    /**
     * 温度参数（0.0-2.0）
     */
    private BigDecimal temperature;
    
    /**
     * Top P参数（0.0-1.0）
     */
    private BigDecimal topP;
    
    /**
     * 频率惩罚（-2.0-2.0）
     */
    private BigDecimal frequencyPenalty;
    
    /**
     * 存在惩罚（-2.0-2.0）
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

