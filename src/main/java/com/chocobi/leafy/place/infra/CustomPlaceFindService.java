package com.chocobi.leafy.place.infra;

import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.place.infra.entity.CustomPlaceEntity;
import com.chocobi.leafy.place.infra.repository.CustomPlaceRepository;
import com.chocobi.leafy.place.vo.PlaceError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomPlaceFindService {
    private final CustomPlaceRepository customPlaceRepository;

    public CustomPlaceEntity getCustomPlace(Long id) {
        return customPlaceRepository.findById(id)
                .orElseThrow(() -> new CustomException(PlaceError.PLACE_NOT_FOUND));
    }
}
