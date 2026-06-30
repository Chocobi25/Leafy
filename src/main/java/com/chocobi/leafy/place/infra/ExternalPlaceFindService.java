package com.chocobi.leafy.place.infra;

import com.chocobi.leafy.global.entity.RegionEntity;
import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.repository.ExternalPlaceRepository;
import com.chocobi.leafy.place.vo.PlaceError;
import com.chocobi.leafy.place.vo.ExternalPlaceStatus;
import java.util.List;
import java.util.Collection;
import com.chocobi.leafy.place.vo.ExternalPlaceSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExternalPlaceFindService {
    private final ExternalPlaceRepository externalPlaceRepository;

    public ExternalPlaceEntity findExternalPlace(Long id) {
        return externalPlaceRepository.findById(id)
                .orElseThrow(() -> new CustomException(PlaceError.PLACE_NOT_FOUND));
    }

    public ExternalPlaceEntity findActiveExternalPlace(Long id) {
        return externalPlaceRepository.findByIdAndStatus(id, ExternalPlaceStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(PlaceError.PLACE_NOT_FOUND));
    }

    public List<ExternalPlaceEntity> findExternalPlaces() {
        return externalPlaceRepository.findAllByStatus(ExternalPlaceStatus.ACTIVE);
    }

    public List<ExternalPlaceEntity> findExternalPlaces(RegionEntity region) {
        return externalPlaceRepository.findAllByRegionAndStatus(region, ExternalPlaceStatus.ACTIVE);
    }

    public List<ExternalPlaceEntity> findExternalPlaces(
            ExternalPlaceSource source,
            Collection<String> externalContentIds
    ) {
        return externalPlaceRepository.findAllBySourceAndExternalContentIdIn(source, externalContentIds);
    }

    public List<String> findActiveExternalContentIds(ExternalPlaceSource source) {
        return externalPlaceRepository.findExternalContentIds(source, ExternalPlaceStatus.ACTIVE);
    }
}
