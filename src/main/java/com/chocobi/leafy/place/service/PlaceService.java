package com.chocobi.leafy.place.service;

import com.chocobi.leafy.place.dto.PlaceDTO;
import com.chocobi.leafy.place.dto.UserPlaceDTO;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.entity.Type;
import com.chocobi.leafy.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
        Optional<Place> existingPlace = placeRepository.findByTitle(userPlaceDTO.getTitle());

        if (existingPlace.isPresent()) {
            return existingPlace.get().getId();
        }

       Place place = Place.builder()
               .address(userPlaceDTO.getAddress())
               .title(userPlaceDTO.getTitle())
               .latitude(userPlaceDTO.getLatitude())
               .longitude(userPlaceDTO.getLongitude())
               .url(userPlaceDTO.getPlaceUrl())
               .tel(userPlaceDTO.getTel())
               .copyright("카카오지도")
               .type(Type.USER)
               .build();

        placeRepository.save(place);
        return place.getId();
    }

    public Place getPlaceById(Long id) {
        Optional<Place> place = placeRepository.findById(id);
        return place.orElse(null);
    }
}
