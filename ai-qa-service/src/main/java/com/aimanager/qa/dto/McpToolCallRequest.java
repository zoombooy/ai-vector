package com.aimanager.qa.dto;

import lombok.Data;

import java.util.Map;

/**
 * MCP工具调用请求
 */
@Data
public class McpToolCallRequest {

    /**
     * MCP Server编码
     */
    private String serverCode;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 调用参数
     */
    private Map<String, Object> arguments;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private Long userId;
}

