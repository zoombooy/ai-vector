package com.aimanager.knowledge.entity;

import com.aimanager.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识分类实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_knowledge_category")
public class KnowledgeCategory extends BaseEntity {
    
    /**
     * 父分类ID
     */
    private Long parentId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 分类编码
     */
    private String categoryCode;
    
    /**
     * 分类类型
     */
    private Integer categoryType;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 描述
     */
    private String description;
}

