package com.aimanager.model.entity;

import com.aimanager.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI模型实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_ai_model")
public class AiModel extends BaseEntity {

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型编码（唯一标识）
     */
    private String modelCode;

    /**
     * 模型类型：openai、baidu、xunfei等
     */
    private String modelType;

    /**
     * 模型版本
     */
    private String modelVersion;

    /**
     * 提供商
     */
    private String provider;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 描述
     */
    private String description;
}

