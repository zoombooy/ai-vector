package com.aimanager.qa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * Agent执行历史实体
 */
@Data
@TableName("t_agent_execution_history")
public class AgentExecutionHistory {
    
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
     * Agent ID
     */
    private Long agentId;
    
    /**
     * Agent编码
     */
    private String agentCode;
    
    /**
     * 输入数据（JSON格式）
     */
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String inputData;

    /**
     * 输出数据（JSON格式）
     */
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String outputData;

    /**
     * 执行状态（SUCCESS/FAILED/TIMEOUT/RUNNING）
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
     * 用户ID
     */
    private Long userId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

