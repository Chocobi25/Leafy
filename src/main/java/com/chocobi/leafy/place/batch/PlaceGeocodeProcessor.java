package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.entity.PlaceStaging;
import com.chocobi.leafy.place.fetcher.kakao.GeocodeService;
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
        log.info("Processing PlaceStaging item with address: {}", item.getAddress());

        double[] coordinates = geocodeService.getCoordinatesFromAddress(item.getAddress());

        if (coordinates == null) {
            log.warn("Failed to get coordinates for address: {}. Skipping item.", item.getAddress());
            return null;
        }

        return Place.builder()
                .title(item.getTitle())
                .description(item.getDescription())
                .category(item.getCategory())
                .address(item.getAddress())
                .latitude(coordinates[0])
                .longitude(coordinates[1])
                .tel(item.getTel())
                .url(item.getUrl())
                .copyright(item.getCopyright())
                .build();
    }
}
