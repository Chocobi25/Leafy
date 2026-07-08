package com.chocobi.leafy.trip.infra.repository;

import com.chocobi.leafy.trip.infra.entity.TripSegmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripSegmentRepository extends JpaRepository<TripSegmentEntity, Long> {
    @Query("""
            select segment
            from TripSegmentEntity segment
            join fetch segment.routeOption routeOption
            join fetch routeOption.trip
            join fetch segment.startTripPlace startTripPlace
            join fetch startTripPlace.place
            join fetch segment.endTripPlace endTripPlace
            join fetch endTripPlace.place
            where routeOption.trip.id = :tripId
              and routeOption.confirmed = true
            order by startTripPlace.dayIndex asc,
                     startTripPlace.visitOrder asc,
                     endTripPlace.dayIndex asc,
                     endTripPlace.visitOrder asc
            """)
    List<TripSegmentEntity> findConfirmedTripSegments(@Param("tripId") Long tripId);

    void deleteAllByRouteOption_Trip_Id(Long tripId);
}
