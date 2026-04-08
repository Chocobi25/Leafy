package com.chocobi.leafy.auth.client;

import com.chocobi.leafy.user.infra.entity.enums.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("target_id_type", "user_id");
        body.add("target_id", providerId);

        kakaoUnlinkWebClient.post()
                .uri("/v1/user/unlink")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
