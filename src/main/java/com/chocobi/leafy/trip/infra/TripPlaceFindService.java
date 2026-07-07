package com.chocobi.leafy.trip.infra;

import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import com.chocobi.leafy.trip.infra.repository.TripPlaceRepository;
import com.chocobi.leafy.trip.vo.TripPlaceError;
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
                .orElseThrow(() -> new CustomException(TripPlaceError.TRIP_PLACE_NOT_FOUND));
    }

    public List<TripPlaceEntity> findTripPlaces(List<Long> tripPlaceIds) {
        List<Long> uniqueTripPlaceIds = tripPlaceIds.stream()
                .distinct()
                .toList();
        List<TripPlaceEntity> tripPlaces = tripPlaceRepository.findAllById(uniqueTripPlaceIds);

        if (tripPlaces.size() != uniqueTripPlaceIds.size()) {
            throw new CustomException(TripPlaceError.TRIP_PLACE_NOT_FOUND);
        }

        return tripPlaces;
    }

    public List<TripPlaceEntity> findOrderedTripPlaces(Long tripId) {
        return tripPlaceRepository.findAllByTripIdOrderByDayIndexAscVisitOrderAsc(tripId);
    }

    public boolean hasTripPlaces(Long tripId) {
        return tripPlaceRepository.existsByTrip_Id(tripId);
    }
}
