package com.chocobi.leafy.auth.service;

import com.chocobi.leafy.auth.entity.RefreshToken;
import com.chocobi.leafy.auth.repository.RefreshTokenRepository;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import com.chocobi.leafy.user.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveRefreshToken(Long userId, String refreshToken, LocalDateTime expiresAt) {
        // TODO: 커스텀 에러로 전환
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 userId입니다."));

        // TODO: 커스텀 에러로 전환
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(rt -> {
                    throw new IllegalStateException("이미 존재하는 토큰입니다.");
                });

        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(expiresAt)
                .build();
        refreshTokenRepository.save(rt);
    }

    public RefreshToken findByToken(String token) {
        // TODO: 커스텀 에러로 전환
        return refreshTokenRepository.findByToken(token).orElseThrow(() -> new IllegalArgumentException("토큰을 찾을 수 없습니다."));
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    @Transactional
    public void deleteAllByUserId(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    @Transactional
    public void rotateRefreshToken(Long userId, String refreshToken, LocalDateTime refreshTokenExpiration) {
        deleteAllByUserId(userId);
        saveRefreshToken(userId, refreshToken, refreshTokenExpiration);
    }
}
