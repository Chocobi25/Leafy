package com.chocobi.leafy.auth.service;

import com.chocobi.leafy.auth.dto.TokenPair;
import com.chocobi.leafy.auth.entity.RefreshToken;
import com.chocobi.leafy.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public TokenPair reissueAccessToken(String refreshToken) {

        // 1. validateToken
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token 입니다."); // TODO: 커스텀 에러로 전환
        }

        // 2. findByToken
        RefreshToken rt = refreshTokenService.findByToken(refreshToken);

        // 3. isExpired
        if (rt.isExpired()) {
            throw new IllegalArgumentException("만료된 Refresh Token 입니다."); // TODO: 커스텀 에러로 전환
        }

        // 4. createAccessToken
        String newAccessToken = jwtUtil.createAccessToken(rt.getUser().getId(), rt.getUser().getRole().getKey());

        // 5. Token Rotation
        String newRefreshToken = jwtUtil.createRefreshToken(rt.getUser().getId());
        rt.rotate(newRefreshToken, jwtUtil.getRefreshTokenExpiration());

        return new TokenPair(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.deleteByToken(refreshToken);
        }
    }
}
