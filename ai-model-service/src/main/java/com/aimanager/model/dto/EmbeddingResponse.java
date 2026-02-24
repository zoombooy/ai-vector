package com.aimanager.model.dto;

import lombok.Data;

import java.util.List;

/**
 * Embedding 响应
 */
@Data
public class EmbeddingResponse {
    
    /**
     * 向量数据
     */
    private List<Float> embedding;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 向量维度
     */
    private Integer dimension;
    
    /**
     * Token 使用量
     */
    private Integer totalTokens;
    
    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;
}

