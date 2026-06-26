package com.yygh.example;

import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 认证示例
 * 演示如何生成和解析 JWT Token
 */
@Component
public class JwtAuthenticationExample {

    // JWT 密钥（实际项目中应从配置文件读取）
    private static final String SECRET = "yygh-secret-key-1234567890123456";
    
    // Token 过期时间（2小时）
    private static final long EXPIRE_TIME = 2 * 60 * 60 * 1000;

    /**
     * 生成 JWT Token
     * @param userId 用户ID
     * @param userName 用户名
     * @return Token 字符串
     */
    public String createToken(Long userId, String userName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userName", userName);
        
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + EXPIRE_TIME);
        
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key)
                .compact();
    }

    /**
     * 从 Token 中获取用户ID
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserId(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            
            Claims claims = claimsJws.getBody();
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }
}
