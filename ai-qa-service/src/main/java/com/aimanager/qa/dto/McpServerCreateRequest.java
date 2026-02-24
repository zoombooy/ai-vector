package com.aimanager.qa.dto;

import lombok.Data;

import java.util.Map;

/**
 * MCP Server创建请求
 */
@Data
public class McpServerCreateRequest {

    /**
     * MCP Server名称
     */
    private String serverName;

    /**
     * MCP Server唯一编码
     */
    private String serverCode;

    /**
     * 描述
     */
    private String description;

    /**
     * MCP Server URL（HTTP Stream地址）
     */
    private String mcpUrl;

    /**
     * 服务器类型（HTTP_STREAM/STDIO/SSE）
     */
    private String serverType;

    /**
     * 环境变量配置
     */
    private Map<String, String> envConfig;

    /**
     * 认证类型（NONE/BEARER/API_KEY）
     */
    private String authType;

    /**
     * 认证配置
     */
    private Map<String, String> authConfig;

    /**
     * 来源（MANUAL/MCPMARKET）
     */
    private String source;

    /**
     * 来源平台的ID
     */
    private String sourceId;

    /**
     * 来源平台的链接
     */
    private String sourceUrl;

    /**
     * Logo图片URL
     */
    private String logoUrl;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（逗号分隔）
     */
    private String tags;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 超时时间（秒）
     */
    private Integer timeout;
}

