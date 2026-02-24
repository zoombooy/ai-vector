package com.aimanager.user.entity;

import com.aimanager.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 组织实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_organization")
public class Organization extends BaseEntity {
    
    /**
     * 组织名称
     */
    private String orgName;
    
    /**
     * 组织编码
     */
    private String orgCode;
    
    /**
     * 父组织ID
     */
    private Long parentId;
    
    /**
     * 组织层级
     */
    private Integer orgLevel;
    
    /**
     * 组织路径
     */
    private String orgPath;
    
    /**
     * 负责人ID
     */
    private Long leaderId;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
}

