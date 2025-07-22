package com.chocobi.leafy.user.Entity;

import com.chocobi.leafy.constants.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.chocobi.leafy.constants.Kakao.CarbonInit;

@Entity // db 테이블 생성
@Table(name = "users") // 테이블명 설정. user는 예약어라 사용 불가
@Getter // lombook을 통해 컴파일 시 getter 자동 설정
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자를 만들어줌. 외부에서는 접근하지 못하게 제한
public class User {

    @Id
    @Column(name = "kakao_id", nullable = false, unique = true) // DB column 설정
    private Long kakaoId;

    @Column(nullable = false, length = 12)
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING) // enum을 문자열로 저장
    @Column(nullable = false) // default 값이 Lv1인 것 저장
    private Level level;

    @Column(name = "total_carbon_saved")
    private double totalCarbonSaved;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public User(Long kakaoId, String nickname, String profileImageUrl) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.role = Role.USER;
        this.level = Level.LV1; // 초기 레벨은 무조건 1
        this.totalCarbonSaved = CarbonInit; // 초기 탄소 절감량 0
    }

    /**
     * data insert 직전에 실행되는 어노테이션
     */
    @PrePersist
    public void creatTime() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
