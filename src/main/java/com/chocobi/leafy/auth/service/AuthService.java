package com.chocobi.leafy.auth.service;

import com.chocobi.leafy.auth.client.OAuthApiClientFactory;
import com.chocobi.leafy.auth.dto.TokenPair;
import com.chocobi.leafy.auth.entity.RefreshToken;
import com.chocobi.leafy.auth.util.JwtUtil;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import com.chocobi.leafy.user.infra.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final OAuthApiClientFactory oAuthApiClientFactory;
    private final UserService userService;

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
        String newAccessToken = jwtUtil.createAccessToken(rt.getUserEntity().getId(), rt.getUserEntity().getRole().getKey());

        // 5. Token Rotation
        String newRefreshToken = jwtUtil.createRefreshToken(rt.getUserEntity().getId());
        rt.rotate(newRefreshToken, jwtUtil.getRefreshTokenExpiration());

        return new TokenPair(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.deleteByToken(refreshToken);
        }
    }

    @Transactional
    public void withdraw(Long userId) {
        UserEntity userEntity = userService.findById(userId);

        // Refresh Token 삭제
        refreshTokenService.deleteAllByUserId(userId);

        // User 삭제
        userService.deleteUser(userId);

        // 소셜 연결 끊기
        oAuthApiClientFactory.getClient(user.getProvider())
                .unlinkUser(user.getProviderId());
    }
}
