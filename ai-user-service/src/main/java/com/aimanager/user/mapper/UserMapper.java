package com.aimanager.user.mapper;

import com.aimanager.user.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}

