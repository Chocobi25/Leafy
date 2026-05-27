package com.chocobi.leafy.trip.infra.repository;

import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripRouteOptionEntity;
import com.chocobi.leafy.trip.vo.TripTransport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRouteOptionRepository extends JpaRepository<TripRouteOptionEntity, Long> {

    List<TripRouteOptionEntity> findAllByTrip_Id(Long tripId);

    Optional<TripRouteOptionEntity> findByTrip_IdAndTransport(Long tripId, TripTransport transport);

    Optional<TripRouteOptionEntity> findByTrip_IdAndConfirmedTrue(Long tripId);

    void deleteAllByTrip(TripEntity trip);
}
