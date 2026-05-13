package com.chocobi.leafy.trip.infra.repository;

import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripSegmentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripSegmentRepository extends JpaRepository<TripSegmentEntity, Long> {
    @EntityGraph(attributePaths = {"trip", "startTripPlace.place", "endTripPlace.place"})
    List<TripSegmentEntity> findByTrip_Id(Long tripId);
    void deleteAllByTrip(TripEntity trip);
}
