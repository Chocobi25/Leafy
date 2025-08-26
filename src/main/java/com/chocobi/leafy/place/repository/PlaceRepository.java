package com.chocobi.leafy.place.repository;

import com.chocobi.leafy.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByAddressContaining(String address);
    List<Place> findByTitle(String title);
}
