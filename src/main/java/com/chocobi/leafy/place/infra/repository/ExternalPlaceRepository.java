package com.chocobi.leafy.place.infra.repository;

import com.chocobi.leafy.global.entity.RegionEntity;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalPlaceRepository extends JpaRepository<ExternalPlaceEntity, Long> {
    List<ExternalPlaceEntity> findAllByRegion(RegionEntity region);
}
