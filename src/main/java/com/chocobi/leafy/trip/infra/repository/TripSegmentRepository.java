package com.chocobi.leafy.trip.infra.repository;

import com.chocobi.leafy.trip.infra.entity.Trip;
import com.chocobi.leafy.trip.infra.entity.TripSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripSegmentRepository extends JpaRepository<TripSegment, Long> {
    List<TripSegment> findByTripId_Id(Long tripId);
    void deleteAllByTripId(Trip trip);
}
