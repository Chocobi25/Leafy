package com.chocobi.leafy.place.infra;

import com.chocobi.leafy.place.infra.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceCommandService {
    private final PlaceRepository placeRepository;

    public void delete(Long placeId) {
        placeRepository.deleteById(placeId);
    }
}
