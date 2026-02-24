package com.aimanager.qa.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Agent视图对象
 */
@Data
public class AgentVO {
    
    /**
     * Agent ID
     */
    private Long id;
    
    /**
     * Agent名称
     */
    private String agentName;
    
    /**
     * Agent编码
     */
    private String agentCode;
    
    /**
     * Agent类型
     */
    private String agentType;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 服务地址
     */
    private String endpointUrl;
    
    /**
     * 输入Schema
     */
    private Map<String, Object> inputSchema;
    
    /**
     * 输出Schema
     */
    private Map<String, Object> outputSchema;
    
    /**
     * 能力列表
     */
    private List<String> capabilities;
    
    /**
     * 超时时间
     */
    private Integer timeout;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 版本
     */
    private String version;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 标签
     */
    private String tags;
    
    /**
     * 优先级
     */
    private Integer priority;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

