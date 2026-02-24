package com.aimanager.qa.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 模型调用请求DTO（用于调用 ai-model-service）
 */
@Data
public class ModelChatRequest {

    private Long modelId;
    private List<ChatMessage> messages;
    private Boolean stream = false;
    private BigDecimal temperature;
    private Integer maxTokens;
    private BigDecimal topP;
    private List<Map<String, Object>> functions;
    private String functionCall;
}

