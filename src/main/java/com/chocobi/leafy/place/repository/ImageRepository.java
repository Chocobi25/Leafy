package com.chocobi.leafy.place.repository;

import com.chocobi.leafy.place.entity.Image;
import com.chocobi.leafy.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    boolean existsByPlace(Place place);
}
