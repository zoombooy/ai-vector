package com.aimanager.auth.controller;

import com.aimanager.auth.dto.LoginRequest;
import com.aimanager.auth.dto.LoginResponse;
import com.aimanager.auth.service.AuthService;
import com.aimanager.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }
    
    /**
     * 验证Token
     */
    @GetMapping("/validate")
    public Result<Boolean> validateToken(@RequestParam String token) {
        boolean valid = authService.validateToken(token);
        return Result.success(valid);
    }
    
    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 实际应用中可以将Token加入黑名单
        return Result.success();
    }
}

