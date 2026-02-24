package com.aimanager.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 角色DTO
 */
@Data
public class RoleDTO {
    
    /**
     * 角色ID（更新时需要）
     */
    private Long id;
    
    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    
    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
    
    /**
     * 角色描述
     */
    private String description;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 状态
     */
    private Integer status;
}

