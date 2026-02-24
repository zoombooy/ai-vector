package com.aimanager.model.dto;

import lombok.Data;

/**
 * 聊天响应DTO
 */
@Data
public class ChatResponse {

    /**
     * 回复内容
     */
    private String content;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 总tokens数
     */
    private Integer totalTokens;

    /**
     * 提示词tokens数
     */
    private Integer promptTokens;

    /**
     * 生成内容tokens数
     */
    private Integer completionTokens;

    /**
     * 完成原因：stop、length、content_filter、null
     */
    private String finishReason;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 函数调用信息（当模型决定调用函数时）
     */
    private FunctionCall functionCall;

    /**
     * 函数调用内部类
     */
    @Data
    public static class FunctionCall {
        /**
         * 函数名称
         */
        private String name;

        /**
         * 函数参数（JSON字符串）
         */
        private String arguments;
    }
}

