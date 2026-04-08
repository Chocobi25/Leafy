package com.chocobi.leafy.auth.client;

import com.chocobi.leafy.user.infra.entity.enums.Provider;

public interface OAuthApiClient {
    Provider getProvider();
    void unlinkUser(String providerId);
}
