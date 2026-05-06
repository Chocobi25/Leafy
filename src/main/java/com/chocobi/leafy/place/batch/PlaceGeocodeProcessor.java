package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceStaging;
import com.chocobi.leafy.place.fetcher.kakao.GeocodeService;
import com.chocobi.leafy.place.fetcher.kakao.dto.GeocodeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceGeocodeProcessor implements ItemProcessor<PlaceStaging, ExternalPlaceEntity> {
    private final GeocodeService geocodeService;

    @Override
    public ExternalPlaceEntity process(PlaceStaging item) throws Exception {
        GeocodeResult geocodeResult = geocodeService.getCoordinatesFromAddress(item.getAddress());

        if (geocodeResult.getAddress() == null) {
            log.warn("Failed to get coordinates for address: {}. Skipping item.", item.getAddress());
            return null;
        }

        return ExternalPlaceEntity.builder()
                .title(item.getTitle())
                .address(geocodeResult.getAddress().getAddress_name())
                .latitude(geocodeResult.getLatitude())
                .longitude(geocodeResult.getLongitude())
                .copyright(item.getCopyright())
                .description(item.getDescription())
                .category(item.getCategory())
                .tel(item.getTel())
                .url(item.getUrl())
                .build();
    }
}
