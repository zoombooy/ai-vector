package com.aimanager.user.mapper;

import com.aimanager.user.entity.Role;
import com.aimanager.user.entity.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    
    /**
     * 根据用户ID查询角色列表
     */
    @Select("SELECT r.* FROM t_role r " +
            "INNER JOIN t_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<Role> selectRolesByUserId(Long userId);
}

