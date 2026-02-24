package com.aimanager.qa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * MCP调用历史实体
 */
@Data
@TableName("t_mcp_call_history")
public class McpCallHistory {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * MCP Server ID
     */
    private Long serverId;

    /**
     * MCP Server编码
     */
    private String serverCode;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 输入数据
     */
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String inputData;

    /**
     * 输出数据
     */
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String outputData;

    /**
     * 执行状态（RUNNING/SUCCESS/FAILED）
     */
    private String status;

    /**
     * 错误信息
     */
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String errorMessage;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行时间（毫秒）
     */
    private Long executionTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

