package com.chocobi.leafy.auth.client;

import com.chocobi.leafy.user.enums.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class KakaoApiClient implements OAuthApiClient{

    private final WebClient kakaoUnlinkWebClient;

    @Override
    public Provider getProvider() {
        return Provider.KAKAO;
    }

    @Override
    public void unlinkUser(String providerId) {
        kakaoUnlinkWebClient.post()
                .uri("/v1/user/unlink")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("target_id_type=user_id&target_id=" + providerId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
