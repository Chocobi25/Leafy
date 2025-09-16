package com.chocobi.leafy.place.service;

import com.chocobi.leafy.place.common.dto.PlaceDTO;
import com.chocobi.leafy.place.common.dto.UserPlaceDTO;
import com.chocobi.leafy.place.entity.Category;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.entity.PlaceSourceType;
import com.chocobi.leafy.place.entity.RegionGroup;
import com.chocobi.leafy.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final PlaceRepository placeRepository;

    public List<PlaceDTO> getPlacesByArrival(String arrival) {
        RegionGroup group = RegionGroup.fromRegionName(arrival);
        List<Place> places = placeRepository.findByRegionGroupAndSourceType(group, PlaceSourceType.API);
        return places.stream()
                .map(PlaceDTO::fromEntity)
                .toList();
    }

    public Place getPlaceById(Long id) {
        if (id == null) {
            return null;
        }
        Optional<Place> place = placeRepository.findById(id);
        return place.orElse(null);
    }

    public List<PlaceDTO> getPlaceBySourceType(PlaceSourceType sourceType) {
        List<Place> places = placeRepository.findBySourceType(sourceType);
        return places.stream()
                .map(PlaceDTO::fromEntity)
                .toList();
    }

    public Long saveUserPlace(UserPlaceDTO userPlaceDTO) {
        if(placeRepository.existsByAddressAndTitle(userPlaceDTO.getAddress(), userPlaceDTO.getTitle())) {
            return placeRepository.findByAddressAndTitle(userPlaceDTO.getAddress(), userPlaceDTO.getTitle()).getId();
        }

        String[] parts = userPlaceDTO.getAddress().split(" ");
        RegionGroup group = RegionGroup.fromRegionName(parts[0]);
        String regionDetail = parts[1];

        return placeRepository.save(Place.builder()
                .title(userPlaceDTO.getTitle())
                .address(userPlaceDTO.getAddress())
                .regionGroup(group)
                .regionDetail(regionDetail)
                .longitude(userPlaceDTO.getLongitude())
                .latitude(userPlaceDTO.getLatitude())
                .tel(userPlaceDTO.getTel())
                .url(userPlaceDTO.getUrl())
                .sourceType(PlaceSourceType.USER)
                .copyright("카카오지도")
                .category(Category.ETC)
                .build()).getId();
    }
}