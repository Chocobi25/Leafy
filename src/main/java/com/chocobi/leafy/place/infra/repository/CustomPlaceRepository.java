package com.chocobi.leafy.place.infra.repository;

import com.chocobi.leafy.place.infra.entity.CustomPlaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomPlaceRepository extends JpaRepository<CustomPlaceEntity, Long> {
}
