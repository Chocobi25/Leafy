package com.chocobi.leafy.trip.infra.repository;

import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripPlaceRepository extends JpaRepository<TripPlaceEntity, Long> {
    void deleteAllByTrip(TripEntity trip);
    List<TripPlaceEntity> findByTrip_Id(Long tripId);
    boolean existsByTrip_Id(Long tripId);

    @EntityGraph(attributePaths = {"place"})
    List<TripPlaceEntity> findAllByTripIdOrderByDayIndexAscVisitOrderAsc(Long tripId);
}
