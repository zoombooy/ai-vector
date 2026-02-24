package com.aimanager.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Embedding 请求
 */
@Data
public class EmbeddingRequest {
    
    /**
     * 模型ID
     */
    @NotNull(message = "模型ID不能为空")
    private Long modelId;
    
    /**
     * 输入文本
     */
    @NotBlank(message = "输入文本不能为空")
    private String input;
}

