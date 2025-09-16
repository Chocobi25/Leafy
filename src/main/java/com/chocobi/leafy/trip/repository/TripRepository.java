package com.chocobi.leafy.trip.repository;

import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByStatusAndCreatedAtBefore(TripStatus status, LocalDateTime createdAt);
    List<Trip> findByUserKakaoIdOrderByCreatedAtDesc(Long kakaoId);
    List<Trip> findAllByStartDateAndStatus(LocalDate startDate, TripStatus status);
}
