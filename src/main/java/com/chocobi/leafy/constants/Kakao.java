package com.chocobi.leafy.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Kakao {
    public static final double CarbonInit = 0;

    @Value("${CLIENT_URI:http://localhost:5173}")
    public String clientUri;

    @Value("${REDIRECT_URI:http://localhost:5173/auth/kakao/callback}")
    public String redirectUri;
}
