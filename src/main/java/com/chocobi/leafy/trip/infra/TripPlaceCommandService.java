package com.chocobi.leafy.trip.infra;

import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import com.chocobi.leafy.trip.infra.repository.TripPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TripPlaceCommandService {
    private final TripPlaceRepository tripPlaceRepository;

    public List<TripPlaceEntity> saveAll(List<TripPlaceEntity> tripPlaceEntities) {
        return tripPlaceRepository.saveAll(tripPlaceEntities);
    }

    public void deleteAll(TripEntity tripEntity) {
        tripPlaceRepository.deleteAllByTrip(tripEntity);
    }
}
