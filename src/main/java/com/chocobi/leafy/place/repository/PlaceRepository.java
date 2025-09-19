package com.chocobi.leafy.place.repository;

import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.entity.PlaceSourceType;
import com.chocobi.leafy.place.entity.RegionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByTitle(String title);
    Place findByAddressAndTitle(String address, String title);
    boolean existsByAddressAndTitle(String address, String title);
    List<Place> findByRegionGroupAndSourceType(RegionGroup regionGroup, PlaceSourceType sourceType);
    List<Place> findByRegionDetail(String regionDetail);
    List<Place> findBySourceType(PlaceSourceType sourceType);
}
