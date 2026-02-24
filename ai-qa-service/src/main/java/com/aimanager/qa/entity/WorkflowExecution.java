package com.aimanager.qa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 工作流执行历史实体
 */
@Data
@TableName("t_workflow_execution")
public class WorkflowExecution {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long workflowId;
    
    private String workflowCode;
    
    private String sessionId;
    
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String inputData;
    
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String outputData;
    
    private String status;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer duration;
    
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String errorMessage;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

