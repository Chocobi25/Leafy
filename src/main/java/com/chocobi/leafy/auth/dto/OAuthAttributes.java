package com.chocobi.leafy.auth.dto;

import com.chocobi.leafy.user.infra.entity.enums.Provider;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * 여러 공급자 파싱 클래스
 */
@Getter
@Builder
public class OAuthAttributes {

    private String providerId;
    private String nickname;
    private String profileImageUrl;
    private Provider provider;
    private Map<String, Object> attributes;
    private String userNameAttributeName;

    // 공급자 분기
    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {

        if ("kakao".equals(registrationId)) {
            return ofKakao(userNameAttributeName, attributes);
        }
        throw new IllegalArgumentException("지원하지 않는 공급자: " + registrationId); // TODO: 커스텀 에러로 변경
    }

    // 카카오 응답 파싱
    @SuppressWarnings("unchecked")
    public static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .providerId(attributes.get("id").toString())
                .nickname(profile.get("nickname").toString())
                .profileImageUrl(profile.get("profile_image_url").toString())
                .provider(Provider.KAKAO)
                .attributes(attributes)
                .userNameAttributeName(userNameAttributeName)
                .build();
    }
}
