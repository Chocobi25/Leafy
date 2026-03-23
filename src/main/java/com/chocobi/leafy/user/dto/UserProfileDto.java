package com.chocobi.leafy.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileDto {
    private String kakaoId;  // TODO: 로직 동작 확인
    private String nickname;
    private String profileImageUrl;
    private String role;
    private String level;
    private double totalCarbonSaved;
    private String selectedLevelIcon;
    private String createdAt;
}
