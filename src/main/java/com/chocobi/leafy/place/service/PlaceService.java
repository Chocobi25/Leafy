package com.chocobi.leafy.place.service;

import com.chocobi.leafy.place.common.dto.PlaceDTO;
import com.chocobi.leafy.place.common.dto.UserPlaceDTO;
import com.chocobi.leafy.place.infra.entity.CustomPlaceEntity;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import com.chocobi.leafy.place.infra.entity.RegionGroup;
import com.chocobi.leafy.place.infra.repository.ImageRepository;
import com.chocobi.leafy.place.infra.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final PlaceRepository placeRepository;
    private final ImageRepository imageRepository;

    /*public List<PlaceDTO> getPlacesByArrival(String arrival) {
        RegionGroup group = RegionGroup.fromRegionName(arrival);
        List<ExternalPlaceEntity> places = placeRepository.findByRegionGroupAndSourceType(group, PlaceSourceType.API);
        return places.stream()
                .map(PlaceDTO::fromEntity)
                .toList();
    }*/

    public PlaceEntity getPlaceById(Long id) {
        if (id == null) {
            return null;
        }
        Optional<PlaceEntity> place = placeRepository.findById(id);
        return place.orElse(null);
    }


    public Long saveUserPlace(UserPlaceDTO userPlaceDTO) {
        if(placeRepository.existsByAddressAndTitle(userPlaceDTO.getAddress(), userPlaceDTO.getTitle())) {
            return placeRepository.findByAddressAndTitle(userPlaceDTO.getAddress(), userPlaceDTO.getTitle()).getId();
        }

        String[] parts = userPlaceDTO.getAddress().split(" ");
        RegionGroup group = RegionGroup.fromRegionName(parts[0]);
        String regionDetail = parts[1];

        return placeRepository.save(CustomPlaceEntity.builder()
                .title(userPlaceDTO.getTitle())
                .address(userPlaceDTO.getAddress())
                .longitude(userPlaceDTO.getLongitude())
                .latitude(userPlaceDTO.getLatitude())
                .copyright("카카오지도")
                .build()).getId();
    }

    // PlaceService.java에 추가할 메서드들

    @Transactional
    public void deletePlace(Long placeId) {
        PlaceEntity place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다: " + placeId));
        placeRepository.delete(place);
    }

    @Transactional
    public void deleteImage(Long imageId) {
        imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다: " + imageId));
        imageRepository.deleteById(imageId);
    }

    public List<PlaceDTO> getAllPlaces() {
        List<PlaceEntity> places = placeRepository.findAll();
        return places.stream()
                .map(PlaceDTO::fromEntity)
                .toList();
    }
}
