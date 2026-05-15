package com.chocobi.leafy.trip.infra;

import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.repository.TripRepository;
import com.chocobi.leafy.trip.vo.TripError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public TripEntity findTripDetail(Long tripId) {
        return tripRepository.findTripById(tripId)
                .orElseThrow(() -> new CustomException(TripError.TRIP_NOT_FOUND));
    }

    public TripEntity findOwnedTrip(Long tripId, Long userId) {
        TripEntity trip = findTrip(tripId);

        if (!trip.getUser().getId().equals(userId)) {
            throw new CustomException(TripError.TRIP_ACCESS_DENIED);
        }

        return trip;
    }

    public TripEntity findOwnedTripDetail(Long tripId, Long userId) {
        TripEntity trip = findTripDetail(tripId);

        if (!trip.getUser().getId().equals(userId)) {
            throw new CustomException(TripError.TRIP_ACCESS_DENIED);
        }

        return trip;
    }

    public List<TripEntity> findTripsByUserId(Long userId) {
        return tripRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
