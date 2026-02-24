package com.aimanager.qa.entity;

import com.aimanager.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问答历史实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_qa_history")
public class QaHistory extends BaseEntity {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 问题
     */
    private String question;

    /**
     * 回答
     */
    private String answer;

    /**
     * 使用的模型ID
     */
    private Long modelId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 使用的tokens数
     */
    private Integer totalTokens;

    /**
     * 用户ID
     */
    private Long userId;
}

