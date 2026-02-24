package com.aimanager.qa.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问答历史VO
 */
@Data
public class QaHistoryVO {
    
    private Long id;
    private String sessionId;
    private String question;
    private String answer;
    private Long modelId;
    private String modelName;
    private Long responseTime;
    private Integer totalTokens;
    private LocalDateTime createTime;
}

