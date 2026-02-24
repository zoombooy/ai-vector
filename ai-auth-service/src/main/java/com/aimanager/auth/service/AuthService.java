package com.aimanager.auth.service;

import com.aimanager.auth.dto.LoginRequest;
import com.aimanager.auth.dto.LoginResponse;
import com.aimanager.auth.entity.User;
import com.aimanager.auth.mapper.UserMapper;
import com.aimanager.common.exception.BusinessException;
import com.aimanager.common.result.ResultCode;
import com.aimanager.common.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .eq(User::getDeleted, 0));
        
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        
        // 查询角色和权限
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        List<String> permissions = userMapper.selectPermissionCodesByUserId(user.getId());
        
        // 生成Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("permissions", permissions);
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), claims);
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRoles(roles);
        response.setPermissions(permissions);
        
        log.info("用户登录成功：{}", user.getUsername());
        return response;
    }
    
    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        return JwtUtil.validateToken(token);
    }
}

