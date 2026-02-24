package com.aimanager.qa.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 问答请求DTO
 */
@Data
public class QaRequest {

    /**
     * 问题
     */
    @NotBlank(message = "问题不能为空")
    private String question;

    /**
     * 会话ID（用于上下文关联）
     */
    private String sessionId;

    /**
     * 知识库ID（可选，指定从哪个知识库检索）
     */
    private Long knowledgeBaseId;

    /**
     * 使用的模型ID（可选）
     */
    private Long modelId;

    /**
     * 是否启用Agent调用（默认false）
     */
    private Boolean enableAgent = false;

    /**
     * 是否启用Function Call（默认true）
     */
    private Boolean enableFunctionCall = true;

    /**
     * 是否启用MCP工具调用（默认false）
     */
    private Boolean enableMcp = false;
}

