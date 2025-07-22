package com.chocobi.leafy.user.service;

import com.chocobi.leafy.user.Entity.User;
import com.chocobi.leafy.user.dto.UserProfileDto;
import com.chocobi.leafy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 만약 유저가 있다면 그 User를 리턴하고, User가 없다면 새 User 객체를 만들어 리턴한다.
     * @param kakaoId
     * @param nickname
     * @param profileImageUrl
     * @return
     */
    public User saveOrGetUser(Long kakaoId, String nickname, String profileImageUrl) {
        return userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> { // Optional<User>이 비어있으면, 안에 있는 함수를 실행해서 값을 새로 만들어 리턴함
                    User newUser = User.builder()
                            .kakaoId(kakaoId)
                            .nickname(nickname)
                            .profileImageUrl(profileImageUrl)
                            .build();
                    return userRepository.save(newUser);
                });
    }


    public void editUser(User user) {
        userRepository.save(user);
    }

    public void carbonSavedUpdate(Long kakaoId, double carbonSaved) {
        userRepository.updateCarbonSaved(kakaoId, carbonSaved);
    }

    @Transactional(readOnly = true) // 읽기 전용
    public UserProfileDto getUserProfile(Long kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with kakaoId: " + kakaoId));

        // User 엔티티를 UserProfileDto로 변환
        return UserProfileDto.builder()
                .kakaoId(user.getKakaoId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole().name())
                .level(user.getLevel().name())
                .totalCarbonSaved(user.getTotalCarbonSaved())
                .build();
    }
}
