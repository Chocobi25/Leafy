package com.chocobi.leafy.trip.infra;

import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import com.chocobi.leafy.trip.infra.repository.TripPlaceRepository;
import com.chocobi.leafy.trip.vo.TripError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripPlaceFindService {
    private final TripPlaceRepository tripPlaceRepository;

    public TripPlaceEntity findTripPlace(Long tripPlaceId) {
        return tripPlaceRepository.findById(tripPlaceId)
                .orElseThrow(() -> new CustomException(TripError.TRIP_PLACE_NOT_FOUND));
    }

    public List<TripPlaceEntity> findTripPlacesByTripId(Long tripId) {
        return tripPlaceRepository.findByTrip_Id(tripId);
    }

    public List<TripPlaceEntity> findTripPlacesOrderByVisitOrder(Long tripId) {
        return tripPlaceRepository.findByTrip_IdOrderByVisitOrderAsc(tripId);
    }
}
