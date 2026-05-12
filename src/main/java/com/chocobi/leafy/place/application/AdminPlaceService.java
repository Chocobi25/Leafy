package com.chocobi.leafy.place.application;

import com.chocobi.leafy.global.service.RegionFindService;
import com.chocobi.leafy.place.dto.request.AdminCreatePlaceRequest;
import com.chocobi.leafy.place.dto.request.AdminUpdateCustomPlaceRequest;
import com.chocobi.leafy.place.dto.request.AdminUpdateExternalPlaceRequest;
import com.chocobi.leafy.place.dto.response.AdminPlaceDetailResponse;
import com.chocobi.leafy.place.dto.response.AdminPlaceListResponse;
import com.chocobi.leafy.place.infra.CategoryService;
import com.chocobi.leafy.place.infra.CustomPlaceFindService;
import com.chocobi.leafy.place.infra.ExternalPlaceCommandService;
import com.chocobi.leafy.place.infra.ExternalPlaceFindService;
import com.chocobi.leafy.place.infra.PlaceCommandService;
import com.chocobi.leafy.place.infra.PlaceFindService;
import com.chocobi.leafy.place.infra.entity.CustomPlaceEntity;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPlaceService {
    private final PlaceFindService placeFindService;
    private final PlaceCommandService placeCommandService;
    private final RegionFindService regionFindService;
    private final CategoryService categoryService;
    private final ExternalPlaceFindService externalPlaceFindService;
    private final ExternalPlaceCommandService externalPlaceCommandService;
    private final CustomPlaceFindService customPlaceFindService;

    @Transactional(readOnly = true)
    public AdminPlaceDetailResponse getPlace(Long placeId) {
        return AdminPlaceDetailResponse.from(placeFindService.findPlace(placeId));
    }

    @Transactional(readOnly = true)
    public List<AdminPlaceListResponse> getPlaces(){
        List<PlaceEntity> places = placeFindService.findAll();
        return places.stream()
                .map(AdminPlaceListResponse::from)
                .toList();
    }

    @Transactional
    public Long createPlace(AdminCreatePlaceRequest request){
        ExternalPlaceEntity place = ExternalPlaceEntity.builder()
                .title(request.title())
                .description(request.description())
                .category(categoryService.findCategory(request.categoryId()))
                .region(regionFindService.findRegion(request.regionId()))
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .tel(request.tel())
                .url(request.url())
                .copyright(request.copyright())
                .build();

        externalPlaceCommandService.save(place);
        return place.getId();
    }

    @Transactional
    public void updateExternalPlace(AdminUpdateExternalPlaceRequest request) {
        ExternalPlaceEntity externalPlace = externalPlaceFindService.findExternalPlace(request.placeId());
        ExternalPlaceEntity updatedPlace = ExternalPlaceEntity.builder()
                .title(request.title())
                .description(request.description())
                .category(categoryService.findCategory(request.categoryId()))
                .region(regionFindService.findRegion(request.regionId()))
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .tel(request.tel())
                .url(request.url())
                .copyright(request.copyright())
                .build();
        externalPlace.update(updatedPlace);
    }

    @Transactional
    public void updateCustomPlace(AdminUpdateCustomPlaceRequest request) {
        CustomPlaceEntity customPlace = customPlaceFindService.getCustomPlace(request.placeId());
        customPlace.update(
                request.title(),
                request.address(),
                request.latitude(),
                request.longitude(),
                request.copyright()
        );
    }

    @Transactional
    public void deletePlace(Long placeId) {
        placeCommandService.delete(placeId);
    }
}
