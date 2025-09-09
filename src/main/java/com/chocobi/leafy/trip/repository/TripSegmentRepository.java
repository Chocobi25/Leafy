package com.chocobi.leafy.trip.repository;

import com.chocobi.leafy.trip.entity.TripSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripSegmentRepository extends JpaRepository<TripSegment, Long> {
    List<TripSegment> findByTripId_Id(Long tripId);
}
