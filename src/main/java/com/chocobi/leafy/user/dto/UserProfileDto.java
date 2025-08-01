package com.chocobi.leafy.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileDto {
    private Long kakaoId;
    private String nickname;
    private String profileImageUrl;
    private String role;
    private String level;
    private double totalCarbonSaved;
    private String selectedLevelIcon;
}
