package com.chocobi.leafy.place.application;

import com.chocobi.leafy.global.entity.RegionEntity;
import com.chocobi.leafy.global.service.RegionFindService;
import com.chocobi.leafy.place.dto.request.CreatePlaceRequest;
import com.chocobi.leafy.place.dto.response.ExternalPlaceListResponse;
import com.chocobi.leafy.place.dto.response.ExternalPlaceDetailResponse;
import com.chocobi.leafy.place.infra.CustomPlaceCommandService;
import com.chocobi.leafy.place.infra.ExternalPlaceFindService;
import com.chocobi.leafy.place.infra.PlaceCommandService;
import com.chocobi.leafy.place.infra.PlaceFindService;
import com.chocobi.leafy.place.infra.entity.CustomPlaceEntity;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final RegionFindService regionFindService;
    private final PlaceFindService placeFindService;
    private final PlaceCommandService placeCommandService;
    private final ExternalPlaceFindService externalPlaceFindService;
    private final CustomPlaceCommandService customPlaceCommandService;

    public PlaceEntity getPlace(Long placeId){
        return placeFindService.findPlace(placeId);
    }

    @Transactional(readOnly = true)
    public ExternalPlaceDetailResponse getExternalPlace(Long id) {
        return ExternalPlaceDetailResponse.from(externalPlaceFindService.findById(id));
    }

    @Transactional(readOnly = true)
    public List<ExternalPlaceListResponse> getExternalPlaces() {
        List<ExternalPlaceEntity> places = externalPlaceFindService.findAll();
        return places.stream()
                .map(ExternalPlaceListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExternalPlaceListResponse> getExternalPlaces(String arrival) {
        RegionEntity region = regionFindService.findRegion(arrival);
        List<ExternalPlaceEntity> places = externalPlaceFindService.findAll(region);
        return places.stream()
                .map(ExternalPlaceListResponse::from)
                .toList();
    }

    @Transactional
    public Long createCustomPlace(CreatePlaceRequest request) {
        CustomPlaceEntity place = CustomPlaceEntity.builder()
                .title(request.title())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .copyright(request.copyright())
                .build();
        customPlaceCommandService.save(place);
        return place.getId();
    }

    @Transactional
    public void deletePlace(Long placeId) {
        placeCommandService.delete(placeId);
    }
}
