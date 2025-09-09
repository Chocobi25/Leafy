package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.entity.PlaceSourceType;
import com.chocobi.leafy.place.entity.PlaceStaging;
import com.chocobi.leafy.place.fetcher.kakao.GeocodeService;
import com.chocobi.leafy.place.fetcher.kakao.dto.GeocodeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceGeocodeProcessor implements ItemProcessor<PlaceStaging, Place> {
    private final GeocodeService geocodeService;

    @Override
    public Place process(PlaceStaging item) throws Exception {
        GeocodeResult geocodeResult = geocodeService.getCoordinatesFromAddress(item.getAddress());

        if (geocodeResult == null) {
            log.warn("Failed to get coordinates for address: {}. Skipping item.", item.getAddress());
            return null;
        }

        return Place.builder()
                .title(item.getTitle())
                .description(item.getDescription())
                .category(item.getCategory())
                .address(geocodeResult.getAddress())
                .latitude(geocodeResult.getLatitude())
                .longitude(geocodeResult.getLongitude())
                .tel(item.getTel())
                .url(item.getUrl())
                .copyright(item.getCopyright())
                .sourceType(PlaceSourceType.API)
                .build();
    }
}
