package com.chocobi.leafy.place.service;

import com.chocobi.leafy.place.dto.PlaceDTO;
import com.chocobi.leafy.place.dto.UserPlaceDTO;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.entity.Type;
import com.chocobi.leafy.place.entity.UserPlace;
import com.chocobi.leafy.place.repository.PlaceRepository;
import com.chocobi.leafy.place.repository.UserPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final PlaceRepository placeRepository;
    private final UserPlaceRepository userPlaceRepository;

    public List<PlaceDTO> getPlaceByAddress(String address) {
        List<Place> places = placeRepository.findByAddressContaining(address);
        return places.stream()
                .map(PlaceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Long saveUserPlace(UserPlaceDTO userPlaceDTO) {
        Optional<UserPlace> existingPlace = userPlaceRepository.findByTitle(userPlaceDTO.getTitle());

        if (existingPlace.isPresent()) {
            return existingPlace.get().getId();
        }

        UserPlace userPlace = UserPlace.builder()
                .address(userPlaceDTO.getAddress())
                .title(userPlaceDTO.getTitle())
                .latitude(userPlaceDTO.getLatitude())
                .longitude(userPlaceDTO.getLongitude())
                .place_url(userPlaceDTO.getPlaceUrl())
                .type(Type.USER)
                .build();

        userPlaceRepository.save(userPlace);
        return userPlace.getId();
    }
}
