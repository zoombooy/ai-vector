package com.aimanager.auth.mapper;

import com.aimanager.auth.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户ID查询角色编码列表
     */
    @Select("SELECT r.role_code FROM t_role r " +
            "INNER JOIN t_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<String> selectRoleCodesByUserId(Long userId);
    
    /**
     * 根据用户ID查询权限编码列表
     */
    @Select("SELECT DISTINCT p.permission_code FROM t_permission p " +
            "INNER JOIN t_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN t_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0")
    List<String> selectPermissionCodesByUserId(Long userId);
}

