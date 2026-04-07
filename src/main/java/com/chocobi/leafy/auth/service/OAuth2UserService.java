package com.chocobi.leafy.auth.service;

import com.chocobi.leafy.auth.dto.OAuthAttributes;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import com.chocobi.leafy.user.infra.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // application-oauth.yml에서 설정한 user-name-attribute 값
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuthAttributes oAuthAttributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        UserEntity userEntity = userService.saveOrGetUser(oAuthAttributes);

        Map<String, Object> attributes = new HashMap<>(oAuthAttributes.getAttributes());
        attributes.put("userId", userEntity.getId());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(userEntity.getRole().getKey())),
                attributes,
                oAuthAttributes.getUserNameAttributeName()
        );
    }
}
