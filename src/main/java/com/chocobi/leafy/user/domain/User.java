package com.chocobi.leafy.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity // db 테이블 생성
@Table(name = "users") // 테이블명 설정. user는 예약어라 사용 불가
@Getter // lombook을 통해 컴파일 시 getter 자동 설정
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자를 만들어줌. 외부에서는 접근하지 못하게 제한
public class User {

    @Id // PK 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 번호 증가 전략 설정
    private Long id;

    @Column(name = "kakao_id", nullable = false, unique = true) // DB column 설정
    private Long kakaoId;

    @Column(nullable = false, length = 12)
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING) // enum을 문자열로 저장
    @Column(nullable = false) // default 값이 Lv1인 것 저장
    private Level level;

    @Column(name = "total_carbon_saved")
    private double totalCarbonSaved;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 필요한 경우에만 setter 추가(보안 문제)
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void addCarbonSaved(double carbonSaved) {
        this.totalCarbonSaved += carbonSaved;
    }

    @Builder
    public User(Long kakaoId, String nickname, String profileImageUrl, Level level, double totalCarbonSaved) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.level = level;
        this.totalCarbonSaved = totalCarbonSaved;
    }

    /**
     * data insert 직전에 실행되는 어노테이션
     */
    @PrePersist
    public void creatTime() {
        this.createdAt = LocalDateTime.now();
    }

    public enum Level {
        LV1, LV2, LV3, LV4, LV5;
    }
}
