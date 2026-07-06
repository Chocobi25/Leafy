package com.chocobi.leafy.trip.infra;

import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripRouteOptionEntity;
import com.chocobi.leafy.trip.infra.repository.TripRouteOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TripRouteOptionCommandService {
    private final TripRouteOptionRepository tripRouteOptionRepository;

    public TripRouteOptionEntity save(TripRouteOptionEntity tripRouteOption) {
        return tripRouteOptionRepository.save(tripRouteOption);
    }

    public void deleteAllByTrip(TripEntity trip) {
        tripRouteOptionRepository.deleteAllByTrip(trip);
    }

    public void delete(TripRouteOptionEntity tripRouteOption) {
        tripRouteOptionRepository.delete(tripRouteOption);
        tripRouteOptionRepository.flush();
    }

    public void confirmOnly(TripRouteOptionEntity selectedRouteOption, List<TripRouteOptionEntity> routeOptions) {
        routeOptions.forEach(TripRouteOptionEntity::unconfirm);
        selectedRouteOption.confirm();
    }
}
