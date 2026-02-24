package com.aimanager.user.service;

import com.aimanager.common.exception.BusinessException;
import com.aimanager.common.result.ResultCode;
import com.aimanager.user.dto.UserDTO;
import com.aimanager.user.entity.Role;
import com.aimanager.user.entity.User;
import com.aimanager.user.entity.UserRole;
import com.aimanager.user.mapper.UserMapper;
import com.aimanager.user.mapper.UserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    
    /**
     * 分页查询用户
     */
    public Page<User> pageUsers(Integer pageNum, Integer pageSize, String keyword, Long organizationId) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getEmail, keyword)
                    .or().like(User::getPhone, keyword));
        }
        
        if (organizationId != null) {
            wrapper.eq(User::getOrganizationId, organizationId);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        
        return userMapper.selectPage(page, wrapper);
    }
    
    /**
     * 根据ID获取用户
     */
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        // 清空密码
        user.setPassword(null);
        return user;
    }
    
    /**
     * 创建用户
     */
    public void createUser(UserDTO userDTO) {
        // 检查用户名是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userDTO.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }
        
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        
        // 加密密码
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        } else {
            // 默认密码
            user.setPassword(passwordEncoder.encode("123456"));
        }
        
        user.setStatus(1); // 默认启用
        userMapper.insert(user);
        
        log.info("创建用户成功: {}", user.getUsername());
    }
    
    /**
     * 更新用户
     */
    public void updateUser(UserDTO userDTO) {
        User existUser = userMapper.selectById(userDTO.getId());
        if (existUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 检查用户名是否被其他用户使用
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userDTO.getUsername())
                .ne(User::getId, userDTO.getId());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }
        
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        
        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        } else {
            user.setPassword(null); // 不更新密码
        }
        
        userMapper.updateById(user);
        
        log.info("更新用户成功: {}", user.getUsername());
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 不能删除管理员
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException(ResultCode.OPERATION_NOT_ALLOWED);
        }
        
        userMapper.deleteById(id);
        
        log.info("删除用户成功: {}", user.getUsername());
    }
    
    /**
     * 重置密码
     */
    public void resetPassword(Long id, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        log.info("重置用户密码成功: {}", user.getUsername());
    }

    /**
     * 获取用户的角色列表
     */
    public List<Role> getUserRoles(Long userId) {
        return userRoleMapper.selectRolesByUserId(userId);
    }

    /**
     * 为用户分配角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 删除用户原有的角色
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        userRoleMapper.delete(wrapper);

        // 分配新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }

        log.info("为用户分配角色成功: userId={}, roleIds={}", userId, roleIds);
    }
}

