package com.chocobi.leafy.place.service;

import com.chocobi.leafy.place.common.dto.PlaceDTO;
import com.chocobi.leafy.place.common.dto.UserPlaceDTO;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.entity.PlaceSourceType;
import com.chocobi.leafy.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final PlaceRepository placeRepository;

    public List<PlaceDTO> getPlaceByAddress(String address) {
        List<Place> places = placeRepository.findByAddressContaining(address);
        return places.stream()
                .map(PlaceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Long saveUserPlace(UserPlaceDTO userPlaceDTO) {
        if(placeRepository.existsByAddressAndTitle(userPlaceDTO.getAddress(), userPlaceDTO.getTitle())) {
            return placeRepository.findByAddressAndTitle(userPlaceDTO.getAddress(), userPlaceDTO.getTitle()).getId();
        }

       return placeRepository.save(Place.builder()
                .title(userPlaceDTO.getTitle())
                .address(userPlaceDTO.getAddress())
                .longitude(Double.parseDouble(userPlaceDTO.getLongitude()))
                .latitude(Double.parseDouble(userPlaceDTO.getLatitude()))
                .tel(userPlaceDTO.getTel())
                .url(userPlaceDTO.getUrl())
                .sourceType(PlaceSourceType.USER)
                .build()).getId();
    }
}
