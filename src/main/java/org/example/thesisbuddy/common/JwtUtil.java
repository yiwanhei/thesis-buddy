package org.example.thesisbuddy.common;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "ThesisBuddy-Admin-JWT-Secret-Key-2026-Very-Long-And-Secure!!".getBytes(StandardCharsets.UTF_8)
    );
    private static final long EXPIRATION = 24 * 60 * 60 * 1000L; // 24小时

    // 生成Token
    public String generateToken(Integer adminId, String account) {
        return Jwts.builder()
                .subject(String.valueOf(adminId))
                .claim("account", account)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();
    }

    // 解析Token，返回Claims（含adminId和account）
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 验证Token是否有效
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 从Token中获取adminId
    public Integer getAdminId(String token) {
        return Integer.parseInt(parseToken(token).getSubject());
    }
}
