package com.aimanager.qa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 工作流节点实体
 */
@Data
@TableName("t_workflow_node")
public class WorkflowNode {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long workflowId;
    
    private String nodeId;
    
    private String nodeName;
    
    private String nodeType;
    
    private Integer positionX;
    
    private Integer positionY;
    
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String config;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

