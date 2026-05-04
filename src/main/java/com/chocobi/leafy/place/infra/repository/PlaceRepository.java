package com.chocobi.leafy.place.infra.repository;

import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import com.chocobi.leafy.place.infra.entity.RegionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<PlaceEntity, Long> {
    List<PlaceEntity> findByTitle(String title);
    PlaceEntity findByAddressAndTitle(String address, String title);
    boolean existsByAddressAndTitle(String address, String title);
}
