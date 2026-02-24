package com.aimanager.qa.entity;

import com.aimanager.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * MCP Server实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_mcp_server")
public class McpServer extends BaseEntity {

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
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
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
     * 环境变量配置（JSON格式）
     */
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String envConfig;

    /**
     * 认证类型（NONE/BEARER/API_KEY）
     */
    private String authType;

    /**
     * 认证配置（JSON格式）
     */
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String authConfig;

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
     * 状态（0-禁用 1-启用）
     */
    private Integer status;

    /**
     * 超时时间（秒）
     */
    private Integer timeout;

    /**
     * 最后健康检查时间
     */
    private LocalDateTime lastHealthCheck;

    /**
     * 健康状态（HEALTHY/UNHEALTHY/UNKNOWN）
     */
    private String healthStatus;
}

