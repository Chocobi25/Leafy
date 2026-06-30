package com.chocobi.leafy.place.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chocobi.leafy.external.tour.client.TourKoreanInfoClient;
import com.chocobi.leafy.external.tour.dto.TourKoreanAreaBasedResponse;
import com.chocobi.leafy.external.tour.dto.TourKoreanDetailImageResponse;
import com.chocobi.leafy.external.tour.dto.TourKoreanPlaceSearchCondition;
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
class TourPlaceSynchronizerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TourKoreanInfoClient tourKoreanInfoClient;

    @Mock
    private ExternalPlaceSyncService externalPlaceSyncService;

    private TourPlaceSynchronizer synchronizer;

    @BeforeEach
    void setUp() {
        synchronizer = new TourPlaceSynchronizer(tourKoreanInfoClient, externalPlaceSyncService);
    }

    @Test
    @DisplayName("실제 페이지 크기로 순회하고 유효 장소의 우선 카테고리만 동기화한다")
    void syncValidPlacesWithCategoryPriority() throws Exception {
        when(tourKoreanInfoClient.fetchAreaBasedPlaces(any(), anyInt(), anyInt()))
                .thenAnswer(invocation -> responseFor(
                        invocation.getArgument(0),
                        invocation.getArgument(1)));
        when(externalPlaceSyncService.deactivateMissing(eq(ExternalPlaceSource.TOUR_API), any()))
                .thenReturn(0);
        when(tourKoreanInfoClient.fetchDetailImages("shared-content"))
                .thenReturn(detailImageResponse());

        int result = synchronizer.sync();

        ArgumentCaptor<List<ExternalPlaceSyncData>> batchCaptor = ArgumentCaptor.forClass(List.class);
        verify(externalPlaceSyncService).syncBatch(batchCaptor.capture());
        List<ExternalPlaceSyncData> syncedPlaces = batchCaptor.getValue();

        assertThat(result).isEqualTo(1);
        assertThat(syncedPlaces).hasSize(1);
        assertThat(syncedPlaces.getFirst().contentId()).isEqualTo("shared-content");
        assertThat(syncedPlaces.getFirst().categoryCode()).isEqualTo("NATURE");
        assertThat(syncedPlaces.getFirst().images()).hasSize(2);
        assertThat(syncedPlaces.getFirst().images().getFirst().url())
                .isEqualTo("https://images.example/thumbnail.jpg");
        assertThat(syncedPlaces.getFirst().images().getFirst().thumbnail()).isTrue();
        assertThat(syncedPlaces.getFirst().images().get(1).url())
                .isEqualTo("https://images.example/detail.jpg");
        assertThat(syncedPlaces.getFirst().images().get(1).thumbnail()).isFalse();
        verify(tourKoreanInfoClient).fetchAreaBasedPlaces(
                new TourKoreanPlaceSearchCondition(null, null, null, "NA", null, null),
                2,
                100);
        verify(externalPlaceSyncService).deactivateMissing(
                ExternalPlaceSource.TOUR_API,
                Set.of("shared-content", "invalid-content"));
    }

    @Test
    @DisplayName("API 수집 실패 시 장소 저장과 비활성화를 실행하지 않는다")
    void doesNotChangePlacesWhenCollectionFails() throws Exception {
        when(tourKoreanInfoClient.fetchAreaBasedPlaces(any(), anyInt(), anyInt()))
                .thenReturn(response("9999", 1, 1, 1, List.of(validItem())));

        assertThatThrownBy(() -> synchronizer.sync())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target=NATURE")
                .hasMessageContaining("page=1");

        verify(externalPlaceSyncService, never()).syncBatch(any());
        verify(externalPlaceSyncService, never()).deactivateMissing(any(), any());
    }

    private TourKoreanAreaBasedResponse responseFor(
            TourKoreanPlaceSearchCondition condition,
            int pageNo
    ) throws Exception {
        if ("NA".equals(condition.lclsSystm1())) {
            if (pageNo == 1) {
                return response("0000", 1, 1, 2, List.of(validItem()));
            }
            return response("0000", 2, 1, 2, List.of(invalidItem()));
        }
        return response("0000", 1, 1, 1, List.of(validItem()));
    }

    private TourKoreanAreaBasedResponse response(
            String resultCode,
            int pageNo,
            int numOfRows,
            int totalCount,
            List<Map<String, Object>> items
    ) throws Exception {
        Map<String, Object> body = Map.of(
                "numOfRows", numOfRows,
                "pageNo", pageNo,
                "totalCount", totalCount,
                "items", Map.of("item", items)
        );
        Map<String, Object> response = Map.of(
                "header", Map.of("resultCode", resultCode, "resultMsg", "result"),
                "body", body
        );
        String json = objectMapper.writeValueAsString(Map.of("response", response));
        return objectMapper.readValue(json, TourKoreanAreaBasedResponse.class);
    }

    private Map<String, Object> validItem() {
        return Map.ofEntries(
                Map.entry("contentid", "shared-content"),
                Map.entry("contenttypeid", "12"),
                Map.entry("title", "유효한 장소"),
                Map.entry("addr1", "서울특별시 종로구"),
                Map.entry("mapx", "126.9"),
                Map.entry("mapy", "37.5"),
                Map.entry("firstimage", "https://images.example/thumbnail.jpg"),
                Map.entry("cpyrhtDivCd", "Type1"),
                Map.entry("modifiedtime", "20260628010101")
        );
    }

    private TourKoreanDetailImageResponse detailImageResponse() throws Exception {
        Map<String, Object> response = Map.of(
                "header", Map.of("resultCode", "0000", "resultMsg", "OK"),
                "body", Map.of(
                        "numOfRows", 100,
                        "pageNo", 1,
                        "totalCount", 1,
                        "items", Map.of("item", List.of(Map.of(
                                "contentid", "shared-content",
                                "originimgurl", "https://images.example/detail.jpg",
                                "cpyrhtDivCd", "Type1"))))
        );
        return objectMapper.readValue(
                objectMapper.writeValueAsString(Map.of("response", response)),
                TourKoreanDetailImageResponse.class);
    }

    private Map<String, Object> invalidItem() {
        return Map.of(
                "contentid", "invalid-content",
                "title", "좌표 없는 장소",
                "addr1", "서울특별시 종로구"
        );
    }
}
