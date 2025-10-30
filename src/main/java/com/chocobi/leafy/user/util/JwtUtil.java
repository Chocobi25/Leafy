package com.chocobi.leafy.user.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationTime;

    // 생성자에서 secretKey와 expirationTime 주입
    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration.time}") long expirationTime) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expirationTime;
    }

    /**
     * 사용자 ID와 역할을 기반으로 JWT를 생성한다.
     * @param userId
     * @param role
     * @return
     */
    public String createToken(Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(userId.toString())  // 토큰의 주체로 사용자 ID를 설정
                .claim("role", role)         // 사용자 역할을 클레임으로 추가
                .issuedAt(now)               // 토큰 발급 시간
                .expiration(expiryDate)      // 토큰 만료 시간
                .signWith(secretKey)  // 서명
                .compact();
    }

    /**
     * 토큰에서 클레임(정보)을 추출한다.
     * @param token
     * @return
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰에서 사용자 ID를 추출한다.
     * @param token
     * @return
     */
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    /**
     * 토큰의 유효성을 검사한다.
     * @param token
     * @return
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // 토큰이 유효하지 않은 경우 (만료, 형식 오류 등)
            return false;
        }
    }

    /**
     * 토큰에서 역할(Role)을 추출한다.
     * @param token
     * @return
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }
}