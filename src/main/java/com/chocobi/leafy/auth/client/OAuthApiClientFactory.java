package com.chocobi.leafy.auth.client;

import com.chocobi.leafy.user.infra.entity.enums.Provider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OAuthApiClientFactory {

    private final Map<Provider, OAuthApiClient> clientMap;

    public OAuthApiClientFactory(List<OAuthApiClient> clients) {
        clientMap = clients.stream()
                .collect(Collectors.toMap(
                        OAuthApiClient::getProvider,
                        client -> client
                ));
    }

    public OAuthApiClient getClient(Provider provider) {
        OAuthApiClient client = clientMap.get(provider);
        if (client == null) {
            throw new IllegalArgumentException("지원하지 않는 PROVIDER 입니다."); // UNSUPPORTED_PROVIDER 에러 추가
        }

        return client;
    }
}
