package com.aimanager.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 聊天请求DTO
 */
@Data
public class ChatRequest {
    
    /**
     * 模型ID
     */
    @NotNull(message = "模型ID不能为空")
    private Long modelId;
    
    /**
     * 消息列表
     */
    @NotEmpty(message = "消息列表不能为空")
    private List<ChatMessage> messages;
    
    /**
     * 是否流式返回
     */
    private Boolean stream = false;
    
    /**
     * 温度（覆盖模型默认配置）
     */
    private BigDecimal temperature;
    
    /**
     * 最大tokens（覆盖模型默认配置）
     */
    private Integer maxTokens;
    
    /**
     * top_p（覆盖模型默认配置）
     */
    private BigDecimal topP;

    /**
     * 可用的函数列表（用于 Function Call）
     */
    private List<Map<String, Object>> functions;

    /**
     * 函数调用模式（auto/none）
     */
    private String functionCall;
}

