package com.aimanager.qa.dto;

import lombok.Data;

/**
 * 模型调用响应DTO（来自 ai-model-service）
 */
@Data
public class ModelChatResponse {

    private String content;
    private String model;
    private Integer totalTokens;
    private Integer promptTokens;
    private Integer completionTokens;
    private String finishReason;
    private Long responseTime;
    private FunctionCall functionCall;

    /**
     * 函数调用信息
     */
    @Data
    public static class FunctionCall {
        private String name;
        private String arguments;
    }
}

