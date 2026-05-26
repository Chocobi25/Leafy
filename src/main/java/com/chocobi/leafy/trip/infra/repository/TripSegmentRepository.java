package com.chocobi.leafy.trip.infra.repository;

import com.chocobi.leafy.trip.infra.entity.TripRouteOptionEntity;
import com.chocobi.leafy.trip.infra.entity.TripSegmentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripSegmentRepository extends JpaRepository<TripSegmentEntity, Long> {
    @EntityGraph(attributePaths = {"routeOption.trip", "startTripPlace.place", "endTripPlace.place"})
    List<TripSegmentEntity> findByRouteOption_Id(Long routeOptionId);

    @EntityGraph(attributePaths = {"routeOption.trip", "startTripPlace.place", "endTripPlace.place"})
    List<TripSegmentEntity> findByRouteOption_Trip_Id(Long tripId);

    @EntityGraph(attributePaths = {"routeOption.trip", "startTripPlace.place", "endTripPlace.place"})
    List<TripSegmentEntity> findByRouteOption_Trip_IdAndRouteOption_ConfirmedTrue(Long tripId);

    void deleteAllByRouteOption(TripRouteOptionEntity routeOption);

    void deleteAllByRouteOption_Trip_Id(Long tripId);
}
