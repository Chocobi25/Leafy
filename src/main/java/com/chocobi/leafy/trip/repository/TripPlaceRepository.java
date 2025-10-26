package com.chocobi.leafy.trip.repository;

import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripPlaceRepository extends JpaRepository<TripPlace, Long> {
    List<TripPlace> findByTripId(Long tripId);
    void deleteAllByTrip(Trip trip);
    List<TripPlace> findByTripIdOrderByVisitOrderAsc(Long tripId);
    List<TripPlace> findByTripId_Id(Long tripId);
}
