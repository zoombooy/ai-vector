package com.aimanager.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 组织DTO
 */
@Data
public class OrganizationDTO {
    
    /**
     * 组织ID（更新时需要）
     */
    private Long id;
    
    /**
     * 组织名称
     */
    @NotBlank(message = "组织名称不能为空")
    private String orgName;
    
    /**
     * 组织编码
     */
    @NotBlank(message = "组织编码不能为空")
    private String orgCode;
    
    /**
     * 父组织ID
     */
    private Long parentId;
    
    /**
     * 负责人ID
     */
    private Long leaderId;
    
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
}

