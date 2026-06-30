package com.chocobi.leafy.place.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.global.service.RegionFindService;
import com.chocobi.leafy.place.infra.CustomPlaceCommandService;
import com.chocobi.leafy.place.infra.ExternalPlaceFindService;
import com.chocobi.leafy.place.infra.PlaceCommandService;
import com.chocobi.leafy.place.infra.PlaceFindService;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.vo.ExternalPlaceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {
    @Mock
    private RegionFindService regionFindService;
    @Mock
    private PlaceFindService placeFindService;
    @Mock
    private PlaceCommandService placeCommandService;
    @Mock
    private ExternalPlaceFindService externalPlaceFindService;
    @Mock
    private CustomPlaceCommandService customPlaceCommandService;

    private PlaceService placeService;

    @BeforeEach
    void setUp() {
        placeService = new PlaceService(
                regionFindService,
                placeFindService,
                placeCommandService,
                externalPlaceFindService,
                customPlaceCommandService);
    }

    @Test
    @DisplayName("비활성 외부 장소는 일반 장소 조회에서 차단한다")
    void rejectsInactiveExternalPlace() {
        ExternalPlaceEntity inactivePlace = ExternalPlaceEntity.builder()
                .title("비활성 장소")
                .address("서울특별시")
                .latitude(37.5)
                .longitude(126.9)
                .status(ExternalPlaceStatus.INACTIVE)
                .build();
        when(placeFindService.findPlace(1L)).thenReturn(inactivePlace);

        assertThatThrownBy(() -> placeService.getPlace(1L))
                .isInstanceOf(CustomException.class)
                .hasMessage("존재하지 않는 장소입니다.");
    }
}
