package com.chocobi.leafy.global.entity;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<RegionEntity, Long> {
    Optional<RegionEntity> findByName(String name);
    Optional<RegionEntity> findByNameAndLevel(String name, RegionLevel level);
    List<RegionEntity> findByLevel(RegionLevel level);
}
