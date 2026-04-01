package com.chocobi.leafy.user.entity;

import com.chocobi.leafy.global.entity.BaseEntity;
import com.chocobi.leafy.user.enums.Level;
import com.chocobi.leafy.user.enums.Provider;
import com.chocobi.leafy.user.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import static com.chocobi.leafy.user.util.LevelCalculator.calculateLevel;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
public class User extends BaseEntity {

    @Column(name = "nickname", nullable = false, length = 12)
    private String nickname;

    @Column(name = "profile_image_url", nullable = false)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Level level = Level.LV1;

    @Column(name = "total_carbon_saved", nullable = false)
    @Builder.Default
    private double totalCarbonSaved = 0.0;

    @Column(name = "selected_level_icon", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Level selectedLevelIcon = Level.LV1;

    @Column(name = "provider", nullable = false)
    private Provider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateSelectedLevelIcon(Level level) {
        this.selectedLevelIcon = level;
    }

    // 테스트용 탄소 절감량 설정 메서드
    public void setTotalCarbonSaved(double totalCarbonSaved) {
        this.totalCarbonSaved = totalCarbonSaved;
        this.level = calculateLevel(totalCarbonSaved); // 레벨 자동 계산
    }
}
