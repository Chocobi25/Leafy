package com.chocobi.leafy.place.infra;

import com.chocobi.leafy.global.entity.RegionEntity;
import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.repository.ExternalPlaceRepository;
import com.chocobi.leafy.place.vo.PlaceError;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExternalPlaceFindService {
    private final ExternalPlaceRepository externalPlaceRepository;

    public ExternalPlaceEntity findById(Long id) {
        return externalPlaceRepository.findById(id)
                .orElseThrow(() -> new CustomException(PlaceError.PLACE_NOT_FOUND));
    }

    public List<ExternalPlaceEntity> findAll() {
        return externalPlaceRepository.findAll();
    }

    public List<ExternalPlaceEntity> findAll(RegionEntity region) {
        return externalPlaceRepository.findAllByRegion(region);
    }
}
