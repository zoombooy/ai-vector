package com.aimanager.qa.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * MCP工具视图对象
 */
@Data
public class McpToolVO {

    private Long id;
    private Long serverId;
    private String serverCode;
    private String serverName;
    private String toolName;
    private String toolCode;
    private String description;
    private Map<String, Object> inputSchema;
    private Integer enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

