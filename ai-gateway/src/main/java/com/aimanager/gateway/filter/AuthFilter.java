package com.aimanager.gateway.filter;

import com.aimanager.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 认证过滤器
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    
    /**
     * 白名单路径
     */
    private static final List<String> WHITE_LIST = List.of(
            "/auth/login",
            "/auth/register"
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 白名单路径直接放行
        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }
        
        // 获取Token
        String token = getToken(request);
        
        // 验证Token
        if (token == null || !JwtUtil.validateToken(token)) {
            log.warn("Token验证失败，路径：{}", path);
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        
        // 将用户信息添加到请求头
        Long userId = JwtUtil.getUserId(token);
        String username = JwtUtil.getUsername(token);
        
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-Username", username)
                .build();
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
    
    /**
     * 判断是否白名单路径
     */
    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 获取Token
     */
    private String getToken(ServerHttpRequest request) {
        List<String> headers = request.getHeaders().get("Authorization");
        if (headers != null && !headers.isEmpty()) {
            String authHeader = headers.get(0);
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        return null;
    }
    
    @Override
    public int getOrder() {
        return -100;
    }
}

