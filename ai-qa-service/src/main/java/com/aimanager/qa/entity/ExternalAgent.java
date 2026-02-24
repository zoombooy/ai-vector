package com.aimanager.qa.entity;

import com.aimanager.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

/**
 * 外部Agent实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_external_agent")
public class ExternalAgent extends BaseEntity {
    
    /**
     * Agent名称
     */
    private String agentName;
    
    /**
     * Agent唯一编码
     */
    private String agentCode;
    
    /**
     * Agent类型（PYTHON/JAVA/HTTP）
     */
    private String agentType;
    
    /**
     * Agent描述
     */
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String description;

    /**
     * Agent服务地址
     */
    private String endpointUrl;

    /**
     * 通信方式（HTTP/GRPC/WEBSOCKET）
     */
    private String endpointType;

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
     * 输入参数Schema（JSON Schema格式）
     */
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String inputSchema;

    /**
     * 输出结果Schema（JSON Schema格式）
     */
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String outputSchema;

    /**
     * Agent能力列表（JSON数组）
     */
    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String capabilities;
    
    /**
     * 超时时间（秒）
     */
    private Integer timeout;
    
    /**
     * 重试次数
     */
    private Integer retryTimes;
    
    /**
     * 最大并发数
     */
    private Integer maxConcurrent;
    
    /**
     * 状态（0-禁用 1-启用）
     */
    private Integer status;
    
    /**
     * Agent版本
     */
    private String version;
    
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
}

