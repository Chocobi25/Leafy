package com.chocobi.leafy.place.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chocobi.leafy.place.common.dto.ExternalPlaceImageSyncData;
import com.chocobi.leafy.place.common.dto.ExternalPlaceSyncData;
import com.chocobi.leafy.place.infra.CategoryFindService;
import com.chocobi.leafy.place.infra.ExternalPlaceCommandService;
import com.chocobi.leafy.place.infra.ExternalPlaceFindService;
import com.chocobi.leafy.place.infra.PlaceImageCommandService;
import com.chocobi.leafy.place.infra.PlaceImageFindService;
import com.chocobi.leafy.place.infra.entity.CategoryEntity;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceImageEntity;
import com.chocobi.leafy.place.vo.ExternalPlaceSource;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExternalPlaceSyncServiceTest {
    @Mock
    private ExternalPlaceFindService externalPlaceFindService;
    @Mock
    private ExternalPlaceCommandService externalPlaceCommandService;
    @Mock
    private CategoryFindService categoryFindService;
    @Mock
    private PlaceImageFindService placeImageFindService;
    @Mock
    private PlaceImageCommandService placeImageCommandService;

    private ExternalPlaceSyncService externalPlaceSyncService;

    @BeforeEach
    void setUp() {
        externalPlaceSyncService = new ExternalPlaceSyncService(
                externalPlaceFindService,
                externalPlaceCommandService,
                categoryFindService,
                placeImageFindService,
                placeImageCommandService);
    }

    @Test
    void savesThumbnailAndDetailImagesForTourPlace() {
        CategoryEntity category = CategoryEntity.builder()
                .code("NATURE")
                .name("자연")
                .build();
        ExternalPlaceSyncData place = tourPlace();
        when(categoryFindService.findCategories(any())).thenReturn(List.of(category));
        when(externalPlaceFindService.findExternalPlaces(any(), any())).thenReturn(List.of());
        when(placeImageFindService.findPlaceImages(any(Collection.class))).thenReturn(List.of());

        externalPlaceSyncService.syncBatch(List.of(place));

        ArgumentCaptor<List<ExternalPlaceEntity>> placeCaptor = ArgumentCaptor.forClass(List.class);
        verify(externalPlaceCommandService).saveAll(placeCaptor.capture());
        assertThat(placeCaptor.getValue()).hasSize(1);

        ArgumentCaptor<List<PlaceImageEntity>> imageCaptor = ArgumentCaptor.forClass(List.class);
        verify(placeImageCommandService).replaceAll(any(), imageCaptor.capture());
        assertThat(imageCaptor.getValue()).hasSize(2);
        assertThat(imageCaptor.getValue().getFirst().getThumbnail()).isTrue();
        assertThat(imageCaptor.getValue().get(1).getThumbnail()).isFalse();
    }

    private ExternalPlaceSyncData tourPlace() {
        return new ExternalPlaceSyncData(
                ExternalPlaceSource.TOUR_API,
                "NATURE",
                "content-1",
                12,
                "관광지",
                "서울특별시 종로구",
                37.5,
                126.9,
                "Type1",
                null,
                null,
                null,
                "NA",
                null,
                null,
                "20260629010101",
                true,
                List.of(
                        new ExternalPlaceImageSyncData("https://images/thumbnail.jpg", "Type1", 0, true),
                        new ExternalPlaceImageSyncData("https://images/detail.jpg", "Type1", 1, false)));
    }
}
