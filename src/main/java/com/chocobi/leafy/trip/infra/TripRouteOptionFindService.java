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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripRouteOptionFindService {
    private final TripRouteOptionRepository tripRouteOptionRepository;

    public TripRouteOptionEntity findTripRouteOption(Long routeOptionId) {
        return tripRouteOptionRepository.findById(routeOptionId)
                .orElseThrow(() -> new CustomException(TripError.TRIP_ROUTE_OPTION_NOT_FOUND));
    }

    public List<TripRouteOptionEntity> findTripRouteOptions(Long tripId) {
        return tripRouteOptionRepository.findAllByTrip_Id(tripId);
    }

    public TripRouteOptionEntity findTripRouteOption(Long tripId, String transport) {
        return tripRouteOptionRepository.findByTrip_IdAndTransport(tripId, TripTransport.from(transport))
                .orElseThrow(() -> new CustomException(TripError.TRIP_ROUTE_OPTION_NOT_FOUND));
    }

    public TripRouteOptionEntity findConfirmedTripRouteOption(Long tripId) {
        return tripRouteOptionRepository.findByTrip_IdAndConfirmedTrue(tripId)
                .orElseThrow(() -> new CustomException(TripError.TRIP_ROUTE_OPTION_NOT_FOUND));
    }
}
