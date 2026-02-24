package com.aimanager.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT工具类
 */
@Slf4j
public class JwtUtil {
    
    /**
     * 密钥（实际使用时应从配置文件读取）
     */
    private static final String SECRET_KEY = "ai-knowledge-platform-secret-key-2024-very-long-secret";
    
    /**
     * 过期时间（7天）
     */
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;
    
    /**
     * 生成密钥
     */
    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 生成Token
     */
    public static String generateToken(Long userId, String username, Map<String, Object> claims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);
        
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSecretKey())
                .compact();
    }
    
    /**
     * 解析Token
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Token解析失败：{}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 验证Token是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims != null && !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 判断Token是否过期
     */
    private static boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
    
    /**
     * 从Token中获取用户ID
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return Long.parseLong(claims.getSubject());
        }
        return null;
    }
    
    /**
     * 从Token中获取用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("username", String.class);
        }
        return null;
    }
}

