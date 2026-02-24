package com.aimanager.user.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 用户DTO
 */
@Data
public class UserDTO {
    
    /**
     * 用户ID（更新时需要）
     */
    private Long id;
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 密码（创建时需要）
     */
    private String password;
    
    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 组织ID
     */
    private Long organizationId;
    
    /**
     * 状态
     */
    private Integer status;
}

