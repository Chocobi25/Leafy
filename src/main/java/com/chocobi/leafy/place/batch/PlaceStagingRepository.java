package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.place.entity.PlaceStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceStagingRepository extends JpaRepository<PlaceStaging, Long> {
}
