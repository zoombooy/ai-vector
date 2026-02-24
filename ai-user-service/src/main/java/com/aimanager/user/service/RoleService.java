package com.aimanager.user.service;

import com.aimanager.common.exception.BusinessException;
import com.aimanager.common.result.ResultCode;
import com.aimanager.user.dto.RoleDTO;
import com.aimanager.user.entity.Role;
import com.aimanager.user.mapper.RoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 角色服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final RoleMapper roleMapper;
    
    /**
     * 分页查询角色
     */
    public Page<Role> pageRoles(Integer pageNum, Integer pageSize, String keyword) {
        Page<Role> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Role::getRoleName, keyword)
                    .or().like(Role::getRoleCode, keyword));
        }
        
        wrapper.orderByAsc(Role::getSort);
        
        return roleMapper.selectPage(page, wrapper);
    }
    
    /**
     * 获取所有角色
     */
    public List<Role> getAllRoles() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getStatus, 1); // 只查询启用的角色
        wrapper.orderByAsc(Role::getSort);
        return roleMapper.selectList(wrapper);
    }
    
    /**
     * 根据ID获取角色
     */
    public Role getRoleById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色不存在");
        }
        return role;
    }
    
    /**
     * 创建角色
     */
    public void createRole(RoleDTO roleDTO) {
        // 检查角色编码是否存在
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, roleDTO.getRoleCode());
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色编码已存在");
        }
        
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        
        if (role.getStatus() == null) {
            role.setStatus(1); // 默认启用
        }
        
        roleMapper.insert(role);
        
        log.info("创建角色成功: {}", role.getRoleName());
    }
    
    /**
     * 更新角色
     */
    public void updateRole(RoleDTO roleDTO) {
        Role existRole = roleMapper.selectById(roleDTO.getId());
        if (existRole == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色不存在");
        }
        
        // 检查角色编码是否被其他角色使用
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, roleDTO.getRoleCode())
                .ne(Role::getId, roleDTO.getId());
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色编码已存在");
        }
        
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        
        roleMapper.updateById(role);
        
        log.info("更新角色成功: {}", role.getRoleName());
    }
    
    /**
     * 删除角色
     */
    public void deleteRole(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色不存在");
        }
        
        // TODO: 检查角色是否被用户使用，如果被使用则不能删除
        
        roleMapper.deleteById(id);
        
        log.info("删除角色成功: {}", role.getRoleName());
    }
}

