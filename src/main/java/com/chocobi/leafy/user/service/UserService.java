package com.chocobi.leafy.user.service;

import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.repository.TripRepository;
import com.chocobi.leafy.user.dto.UserTripDto;
import com.chocobi.leafy.user.entity.Level;
import com.chocobi.leafy.user.entity.User;
import com.chocobi.leafy.user.dto.UserProfileDto;
import com.chocobi.leafy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;

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

    public User findByKakaoId(Long kakaoId) {
        return userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
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
                .selectedLevelIcon(user.getSelectedLevelIcon().name())
                .totalCarbonSaved(user.getTotalCarbonSaved())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }

    @Transactional
    public void updateSelectedLevelIcon(Long kakaoId, String selectedLevelIcon) {

        User user = findByKakaoId(kakaoId);

        Level iconLevel;
        try {
            iconLevel = Level.valueOf(selectedLevelIcon);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 아이콘입니다.");
        }

        // 레벨 검증(사용자 레벨보다 높은 아이콘 선택 방지)
        if (!isValidIconSelection(user.getLevel(), iconLevel)) {
            throw new IllegalArgumentException("선택할 수 없는 아이콘입니다.");
        }

        user.updateSelectedLevelIcon(iconLevel);
    }

    // 레벨 검증 메소드
    private boolean isValidIconSelection(Level userLevel, Level selectedLevelIcon) {
        // Level의 enum의 ordinal() 값으로 비교 (LV1=0, LV2=1, .... LV5=4)
        return selectedLevelIcon.ordinal() <= userLevel.ordinal();
    }

    @Transactional(readOnly = true)
    public List<UserTripDto> getUserTrips(Long kakaoId) {
        List<Trip> trips = tripRepository.findByUserKakaoIdOrderByCreatedAtDesc(kakaoId);
        
        return trips.stream()
                .map(trip -> UserTripDto.builder()
                        .tripId(trip.getId())
                        .title(trip.getTitle())
                        .startDate(trip.getStartDate())
                        .endDate(trip.getEndDate())
                        .carbonSaved(trip.getCarbonSaved())
                        .carbonEmission(trip.getCarbonEmission())
                        .status(trip.getStatus())
                        .createdAt(trip.getCreatedAt())
                        .totalPlaces(trip.getTripPlaces() != null ? trip.getTripPlaces().size() : 0)
                        .build())
                .collect(Collectors.toList());
    }
}
