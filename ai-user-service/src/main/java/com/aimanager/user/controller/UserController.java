package com.aimanager.user.controller;

import com.aimanager.common.result.Result;
import com.aimanager.user.dto.UserDTO;
import com.aimanager.user.entity.Role;
import com.aimanager.user.entity.User;
import com.aimanager.user.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 分页查询用户
     */
    @GetMapping("/page")
    public Result<Page<User>> pageUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long organizationId) {
        
        Page<User> page = userService.pageUsers(pageNum, pageSize, keyword, organizationId);
        return Result.success(page);
    }
    
    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return Result.success(user);
    }
    
    /**
     * 创建用户
     */
    @PostMapping
    public Result<Void> createUser(@Validated @RequestBody UserDTO userDTO) {
        userService.createUser(userDTO);
        return Result.success();
    }
    
    /**
     * 更新用户
     */
    @PutMapping
    public Result<Void> updateUser(@Validated @RequestBody UserDTO userDTO) {
        userService.updateUser(userDTO);
        return Result.success();
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(
            @PathVariable Long id,
            @RequestParam(defaultValue = "123456") String newPassword) {
        userService.resetPassword(id, newPassword);
        return Result.success();
    }

    /**
     * 获取用户的角色列表
     */
    @GetMapping("/{id}/roles")
    public Result<List<Role>> getUserRoles(@PathVariable Long id) {
        List<Role> roles = userService.getUserRoles(id);
        return Result.success(roles);
    }

    /**
     * 为用户分配角色
     */
    @PostMapping("/{id}/roles")
    public Result<Void> assignRoles(
            @PathVariable Long id,
            @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.success();
    }
}

