package com.aimanager.qa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * Agent执行请求
 */
@Data
public class AgentExecuteRequest {
    
    /**
     * Agent编码
     */
    @NotBlank(message = "Agent编码不能为空")
    private String agentCode;
    
    /**
     * 输入参数
     */
    private Map<String, Object> input;
    
    /**
     * 会话ID（可选）
     */
    private String sessionId;
    
    /**
     * 用户ID（可选）
     */
    private Long userId;
    
    /**
     * 是否异步执行
     */
    private Boolean async = false;
}

