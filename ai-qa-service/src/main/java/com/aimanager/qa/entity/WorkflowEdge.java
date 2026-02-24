package com.aimanager.qa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 工作流连线实体
 */
@Data
@TableName("t_workflow_edge")
public class WorkflowEdge {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long workflowId;
    
    private String edgeId;
    
    private String sourceNodeId;
    
    private String targetNodeId;
    
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String condition;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

