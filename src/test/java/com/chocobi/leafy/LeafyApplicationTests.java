package com.chocobi.leafy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.chocobi.leafy.global.service.RegionInitializer;

@SpringBootTest
@ActiveProfiles("test")
class LeafyApplicationTests {
	@MockitoBean
	private RegionInitializer regionInitializer;

	@Test
	void contextLoads() {
	}

}
