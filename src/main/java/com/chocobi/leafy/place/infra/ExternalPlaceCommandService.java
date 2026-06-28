package com.chocobi.leafy.place.infra;

import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.repository.ExternalPlaceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ExternalPlaceCommandService {
    private final ExternalPlaceRepository externalPlaceRepository;

    public void save(ExternalPlaceEntity externalPlaceEntity) {
        externalPlaceRepository.save(externalPlaceEntity);
    }

    public void saveAll(List<ExternalPlaceEntity> externalPlaces) {
        externalPlaceRepository.saveAll(externalPlaces);
    }
}
