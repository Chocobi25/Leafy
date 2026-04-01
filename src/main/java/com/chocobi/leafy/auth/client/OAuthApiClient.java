package com.chocobi.leafy.auth.client;

import com.chocobi.leafy.user.enums.Provider;

public interface OAuthApiClient {
    Provider getProvider();
    void unlinkUser(String providerId);
}
