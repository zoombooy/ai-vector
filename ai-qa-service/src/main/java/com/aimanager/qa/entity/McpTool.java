package com.aimanager.qa.entity;

import com.aimanager.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

/**
 * MCP工具实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_mcp_tool")
public class McpTool extends BaseEntity {

    /**
     * 所属MCP Server ID
     */
    private Long serverId;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 工具唯一编码（在Server范围内唯一）
     */
    private String toolCode;

    /**
     * 工具描述
     */
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String description;

    /**
     * 输入参数Schema（JSON Schema格式）
     */
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String inputSchema;

    /**
     * 是否启用（0-禁用 1-启用）
     */
    private Integer enabled;
}

