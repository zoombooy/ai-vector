package com.aimanager.qa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 工作流定义实体
 */
@Data
@TableName("t_workflow")
public class Workflow {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String workflowName;
    
    private String workflowCode;
    
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String description;
    
    private String category;
    
    private String tags;
    
    private Integer status;
    
    private String version;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    private String createBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    private String updateBy;
    
    @TableLogic
    private Integer deleted;
}

