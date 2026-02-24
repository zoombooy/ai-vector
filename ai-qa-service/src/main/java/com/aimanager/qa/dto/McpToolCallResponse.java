package com.aimanager.qa.dto;

import lombok.Data;

import java.util.Map;

/**
 * MCP工具调用响应
 */
@Data
public class McpToolCallResponse {

    /**
     * 执行ID
     */
    private Long executionId;

    /**
     * 服务器编码
     */
    private String serverCode;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 执行状态（SUCCESS/FAILED）
     */
    private String status;

    /**
     * 执行结果
     */
    private Object result;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行时间（毫秒）
     */
    private Long executionTime;
}

