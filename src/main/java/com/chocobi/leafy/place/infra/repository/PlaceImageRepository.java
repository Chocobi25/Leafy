package com.chocobi.leafy.place.infra.repository;

import com.chocobi.leafy.place.infra.entity.PlaceImageEntity;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceImageRepository extends JpaRepository<PlaceImageEntity, Long> {
    boolean existsByPlace(ExternalPlaceEntity place);
    List<PlaceImageEntity> findAllById(Long placeId);
    void deleteAllByPlace(ExternalPlaceEntity place);
}
