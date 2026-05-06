package com.chocobi.leafy.place.infra.repository;

import com.chocobi.leafy.place.infra.entity.Image;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    boolean existsByPlace(ExternalPlaceEntity place);
}
