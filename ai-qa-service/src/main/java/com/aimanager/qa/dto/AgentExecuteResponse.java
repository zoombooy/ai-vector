package com.aimanager.qa.dto;

import lombok.Data;

import java.util.Map;

/**
 * Agent执行响应
 */
@Data
public class AgentExecuteResponse {
    
    /**
     * 执行ID
     */
    private Long executionId;
    
    /**
     * Agent编码
     */
    private String agentCode;
    
    /**
     * 执行状态（SUCCESS/FAILED/TIMEOUT/RUNNING）
     */
    private String status;
    
    /**
     * 输出数据
     */
    private Map<String, Object> output;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 执行时间（毫秒）
     */
    private Long executionTime;
    
    /**
     * 创建时间
     */
    private String createTime;
}

