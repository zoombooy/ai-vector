package com.aimanager.user.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 组织树形结构VO
 */
@Data
public class OrganizationTreeVO {
    
    /**
     * 组织ID
     */
    private Long id;
    
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
     * 负责人ID
     */
    private Long leaderId;
    
    /**
     * 负责人姓名
     */
    private String leaderName;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 子组织列表
     */
    private List<OrganizationTreeVO> children;
}

