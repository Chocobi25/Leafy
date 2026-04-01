package com.chocobi.leafy.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

import static java.time.temporal.ChronoUnit.MILLIS;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token.expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token.expiration}") long refreshTokenExpiration
            ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * AccessToken 생성
     * @param userId
     * @param role
     * @return
     */
    public String createAccessToken(Long userId, String role) {
        return buildToken(userId, role, accessTokenExpiration);
    }

    /**
     * RefreshToken 생성
     * @param userId
     * @return
     */
    public String createRefreshToken(Long userId) {
        return buildToken(userId, null, refreshTokenExpiration);
    }

    /**
     * userId, role, expiration으로 token을 만들어서 반환
     * @param userId the ID of the user for whom the token is being created
     * @param role the role of the user to be included in the token (can be null)
     * @param expiration the expiration time for the token in milliseconds
     * @return a compact JWT token as a String
     */
    private String buildToken(Long userId, String role, long expiration) {
        Date now = new Date();
        JwtBuilder builder = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(secretKey);

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.compact();
    }

    /**
     * DB 저장용 RefreshToken 만료 시간 반환
     * @return
     */
    public LocalDateTime getRefreshTokenExpiration() {
        return LocalDateTime.now().plus(refreshTokenExpiration, MILLIS);
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