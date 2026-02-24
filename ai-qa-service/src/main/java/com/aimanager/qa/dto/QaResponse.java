package com.aimanager.qa.dto;

import lombok.Data;

import java.util.List;

/**
 * 问答响应DTO
 */
@Data
public class QaResponse {

    /**
     * 回答
     */
    private String answer;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 相关文档ID列表
     */
    private List<Long> relatedDocuments;

    /**
     * 置信度（0-1）
     */
    private Double confidence;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 调用的Agent列表
     */
    private List<String> calledAgents;

    /**
     * 调用的Function列表
     */
    private List<String> calledFunctions;

    /**
     * 调用的MCP工具列表
     */
    private List<String> calledMcpTools;
}

