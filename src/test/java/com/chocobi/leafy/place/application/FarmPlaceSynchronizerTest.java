package com.chocobi.leafy.place.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chocobi.leafy.external.farm.client.FarmRestaurantClient;
import com.chocobi.leafy.external.farm.dto.FarmRestaurantDetailResponse;
import com.chocobi.leafy.external.farm.dto.FarmRestaurantListResponse;
import com.chocobi.leafy.external.kakao.client.GeocodeClient;
import com.chocobi.leafy.external.kakao.dto.GeocodeResponse.Address;
import com.chocobi.leafy.external.kakao.dto.GeocodedAddress;
import com.chocobi.leafy.place.common.dto.ExternalPlaceSyncData;
import com.chocobi.leafy.place.batch.ExternalPlaceSyncService;
import com.chocobi.leafy.place.vo.ExternalPlaceSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FarmPlaceSynchronizerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FarmRestaurantClient farmRestaurantClient;
    @Mock
    private GeocodeClient geocodeClient;
    @Mock
    private ExternalPlaceSyncService externalPlaceSyncService;

    private FarmPlaceSynchronizer synchronizer;

    @BeforeEach
    void setUp() {
        synchronizer = new FarmPlaceSynchronizer(
                farmRestaurantClient,
                geocodeClient,
                externalPlaceSyncService);
    }

    @Test
    @DisplayName("농가맛집을 전체 페이지에서 수집하고 유효한 장소만 음식 카테고리로 동기화한다")
    void syncValidFarmRestaurants() throws Exception {
        when(farmRestaurantClient.fetchFarmList(1, 500))
                .thenReturn(listResponse(1, 1, 2, List.of(listItem("farm-1", "농가맛집"))));
        when(farmRestaurantClient.fetchFarmList(2, 500))
                .thenReturn(listResponse(2, 1, 2, List.of(listItem("farm-2", "무효 장소"))));
        when(farmRestaurantClient.fetchFarmDetail("farm-1")).thenReturn(detailResponse());
        when(farmRestaurantClient.fetchFarmDetail("farm-2")).thenReturn(invalidDetailResponse());
        when(geocodeClient.getCoordinatesFromAddress("전북 전주시 농가길 1"))
                .thenReturn(geocodedAddress());
        when(externalPlaceSyncService.deactivateMissing(eq(ExternalPlaceSource.FARM_API), any()))
                .thenReturn(0);

        int result = synchronizer.sync();

        ArgumentCaptor<List<ExternalPlaceSyncData>> batchCaptor = ArgumentCaptor.forClass(List.class);
        verify(externalPlaceSyncService).syncBatch(batchCaptor.capture());
        ExternalPlaceSyncData place = batchCaptor.getValue().getFirst();
        assertThat(result).isEqualTo(1);
        assertThat(place.source()).isEqualTo(ExternalPlaceSource.FARM_API);
        assertThat(place.categoryCode()).isEqualTo("FOOD");
        assertThat(place.contentId()).isEqualTo("farm-1");
        assertThat(place.version()).hasSize(64);
        verify(externalPlaceSyncService).deactivateMissing(
                ExternalPlaceSource.FARM_API,
                Set.of("farm-1", "farm-2"));
    }

    private FarmRestaurantListResponse listResponse(
            int pageNo,
            int numOfRows,
            int totalCount,
            List<Map<String, Object>> items
    ) throws Exception {
        Map<String, Object> response = Map.of(
                "header", Map.of("resultCode", "00", "resultMsg", "NORMAL SERVICE"),
                "body", Map.of("items", Map.of(
                        "item", items,
                        "pageNo", pageNo,
                        "numOfRows", numOfRows,
                        "totalCount", totalCount))
        );
        return objectMapper.readValue(
                objectMapper.writeValueAsString(response),
                FarmRestaurantListResponse.class);
    }

    private FarmRestaurantDetailResponse detailResponse() throws Exception {
        Map<String, Object> response = Map.of(
                "header", Map.of("resultCode", "00", "resultMsg", "NORMAL SERVICE"),
                "body", Map.of("item", Map.of(
                        "cntntsNo", "farm-1",
                        "cntntsSj", "농가맛집",
                        "locplc", "전북 전주시 농가길 1",
                        "telno", "063-000-0000",
                        "url", "https://example.com/farm-1",
                        "smm", "지역 농산물 음식점"))
        );
        return objectMapper.readValue(
                objectMapper.writeValueAsString(response),
                FarmRestaurantDetailResponse.class);
    }

    private FarmRestaurantDetailResponse invalidDetailResponse() throws Exception {
        Map<String, Object> response = Map.of(
                "header", Map.of("resultCode", "00", "resultMsg", "NORMAL SERVICE"),
                "body", Map.of("item", Map.of(
                        "cntntsNo", "farm-2",
                        "cntntsSj", "무효 장소",
                        "locplc", ""))
        );
        return objectMapper.readValue(
                objectMapper.writeValueAsString(response),
                FarmRestaurantDetailResponse.class);
    }

    private Map<String, Object> listItem(String contentId, String title) {
        return Map.of("cntntsNo", contentId, "cntntsSj", title, "thumbImgUrl", "https://example.com/image.jpg");
    }

    private GeocodedAddress geocodedAddress() {
        Address address = new Address();
        address.setAddress_name("전북특별자치도 전주시 농가길 1");
        return new GeocodedAddress(35.8, 127.1, address);
    }
}
