package com.aimanager.user.controller;

import com.aimanager.common.result.Result;
import com.aimanager.user.dto.RoleDTO;
import com.aimanager.user.entity.Role;
import com.aimanager.user.service.RoleService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * 分页查询角色
     */
    @GetMapping("/page")
    public Result<Page<Role>> pageRoles(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        
        Page<Role> page = roleService.pageRoles(pageNum, pageSize, keyword);
        return Result.success(page);
    }
    
    /**
     * 获取所有角色
     */
    @GetMapping("/all")
    public Result<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return Result.success(roles);
    }
    
    /**
     * 根据ID获取角色
     */
    @GetMapping("/{id}")
    public Result<Role> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        return Result.success(role);
    }
    
    /**
     * 创建角色
     */
    @PostMapping
    public Result<Void> createRole(@Validated @RequestBody RoleDTO roleDTO) {
        roleService.createRole(roleDTO);
        return Result.success();
    }
    
    /**
     * 更新角色
     */
    @PutMapping
    public Result<Void> updateRole(@Validated @RequestBody RoleDTO roleDTO) {
        roleService.updateRole(roleDTO);
        return Result.success();
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }
}

