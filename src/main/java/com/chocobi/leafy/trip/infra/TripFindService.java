package com.chocobi.leafy.trip.infra;

import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripStatus;
import com.chocobi.leafy.trip.infra.repository.TripRepository;
import com.chocobi.leafy.trip.vo.TripError;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripFindService {
    private final TripRepository tripRepository;

    public TripEntity findTrip(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(TripError.TRIP_NOT_FOUND));
    }

    public List<TripEntity> findTripsByUserId(Long userId) {
        return tripRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
