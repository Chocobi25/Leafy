package com.chocobi.leafy.trip.repository;

import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByStatusAndCreatedAtBefore(TripStatus status, LocalDateTime createdAt);
    List<Trip> findByStatusAndStartDate(TripStatus status, LocalDate startDate);
    List<Trip> findByStatusAndEndDate(TripStatus status, LocalDate endDate);

    @Query("""
        SELECT t FROM Trip t 
        WHERE t.status = :status
        AND t.start_date <= :today
        AND t.end_date >= :today
        AND t.certificationAt IS NULL
    """)
    List<Trip> findActiveTripsWithoutCertification(@Param("status") TripStatus status, @Param("today") LocalDate today);
}
