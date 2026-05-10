package com.chocobi.leafy.place.infra;

import com.chocobi.leafy.place.infra.entity.CustomPlaceEntity;
import com.chocobi.leafy.place.infra.repository.CustomPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomPlaceCommandService {
    private final CustomPlaceRepository customPlaceRepository;

    public void save(CustomPlaceEntity customPlaceEntity) {
        customPlaceRepository.save(customPlaceEntity);
    }
}
