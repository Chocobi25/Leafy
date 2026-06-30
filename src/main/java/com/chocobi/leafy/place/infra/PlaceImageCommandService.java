package com.chocobi.leafy.place.infra;

import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceImageEntity;
import com.chocobi.leafy.place.infra.repository.PlaceImageRepository;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceImageCommandService {
    private final PlaceImageRepository placeImageRepository;

    public void save(PlaceImageEntity placeImage){
        placeImageRepository.save(placeImage);
    }

    public void delete(Long imageId){
        placeImageRepository.deleteById(imageId);
    }

    public void deleteAll(ExternalPlaceEntity place) {
        placeImageRepository.deleteAllByPlace(place);
    }

    public void replaceAll(
            Collection<ExternalPlaceEntity> places,
            List<PlaceImageEntity> placeImages
    ) {
        if (places.isEmpty()) {
            return;
        }
        placeImageRepository.deleteAllByPlaceIn(places);
        placeImageRepository.flush();
        placeImageRepository.saveAll(placeImages);
    }
}
