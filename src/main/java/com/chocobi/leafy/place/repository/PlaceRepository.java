package com.chocobi.leafy.place.repository;

import com.chocobi.leafy.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {
}
