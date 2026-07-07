package com.chocobi.leafy.trip.infra;

import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.trip.infra.entity.TripRouteOptionEntity;
import com.chocobi.leafy.trip.infra.repository.TripRouteOptionRepository;
import com.chocobi.leafy.trip.vo.TripError;
import com.chocobi.leafy.trip.vo.TripTransport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripRouteOptionFindService {
    private final TripRouteOptionRepository tripRouteOptionRepository;

    public List<TripRouteOptionEntity> findTripRouteOptions(Long tripId) {
        return tripRouteOptionRepository.findAllByTrip_Id(tripId);
    }

    public TripRouteOptionEntity findRouteCandidate(Long tripId, String transport) {
        return tripRouteOptionRepository.findByTrip_IdAndTransportAndConfirmedFalse(tripId, TripTransport.from(transport))
                .orElseThrow(() -> new CustomException(TripError.TRIP_ROUTE_OPTION_NOT_FOUND));
    }

    public Optional<TripRouteOptionEntity> findOptionalRouteCandidate(Long tripId, TripTransport transport) {
        return tripRouteOptionRepository.findByTrip_IdAndTransportAndConfirmedFalse(tripId, transport);
    }

}
