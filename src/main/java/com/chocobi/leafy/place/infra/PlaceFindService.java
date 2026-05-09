package com.chocobi.leafy.place.infra;

import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import com.chocobi.leafy.place.infra.repository.PlaceRepository;
import com.chocobi.leafy.place.vo.PlaceError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceFindService {
    private final PlaceRepository placeRepository;

    public PlaceEntity findPlace(Long id) {
        return placeRepository.findById(id)
                .orElseThrow(() -> new CustomException(PlaceError.PLACE_NOT_FOUND));
    }
}
