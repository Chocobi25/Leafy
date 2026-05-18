package com.chocobi.leafy.place.infra;

import com.chocobi.leafy.place.infra.entity.PlaceImageEntity;
import com.chocobi.leafy.place.infra.repository.PlaceImageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceImageFindService {
    private final PlaceImageRepository placeImageRepository;

    public List<PlaceImageEntity> findPlaceImages(Long placeId) {
        return placeImageRepository.findAllById(placeId);
    }
}
