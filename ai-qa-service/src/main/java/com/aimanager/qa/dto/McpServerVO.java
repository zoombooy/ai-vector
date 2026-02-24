package com.aimanager.qa.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * MCP Server视图对象
 */
@Data
public class McpServerVO {

    private Long id;
    private String serverName;
    private String serverCode;
    private String description;
    private String mcpUrl;
    private String serverType;
    private Map<String, String> envConfig;
    private String authType;
    private String source;
    private String sourceId;
    private String sourceUrl;
    private String logoUrl;
    private String category;
    private String tags;
    private Integer priority;
    private Integer status;
    private Integer timeout;
    private LocalDateTime lastHealthCheck;
    private String healthStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 该Server提供的工具列表
     */
    private List<McpToolVO> tools;
}

