package com.chocobi.leafy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.chocobi.leafy.auth.handler.OAuth2SuccessHandler;
import com.chocobi.leafy.global.service.RegionInitializer;

@SpringBootTest
@ActiveProfiles("test")
class LeafyApplicationTests {
	@MockitoBean
	private RegionInitializer regionInitializer;

	@MockitoBean
	private OAuth2SuccessHandler oAuth2SuccessHandler;

	@MockitoBean
	private ClientRegistrationRepository clientRegistrationRepository;

	@Test
	void contextLoads() {
	}

}
