package com.chocobi.leafy.trip.repository;

import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripStatus;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByStatusAndCreatedAtBefore(TripStatus status, LocalDateTime createdAt);
    List<Trip> findByUserIdOrderByCreatedAtDesc(Long id);  // TODO: 로직 동작 확인
    List<Trip> findAllByStartDateAndStatus(LocalDate startDate, TripStatus status);
    List<Trip> findAllByUser(UserEntity user);
}
