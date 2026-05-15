package com.chocobi.leafy.trip.infra.repository;

import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripStatus;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, Long> {
    @EntityGraph(attributePaths = {"departure", "arrival", "user"})
    Optional<TripEntity> findTripById(Long id);

    List<TripEntity> findByStatusAndCreatedAtBefore(TripStatus status, LocalDateTime createdAt);
    List<TripEntity> findByUserIdOrderByCreatedAtDesc(Long id);  // TODO: 로직 동작 확인
    List<TripEntity> findAllByStartDateAndStatus(LocalDate startDate, TripStatus status);
    List<TripEntity> findAllByUser(UserEntity user);
}
