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

    public TripPlaceEntity save(TripPlaceEntity tripPlaceEntity) {
        return tripPlaceRepository.save(tripPlaceEntity);
    }

    public List<TripPlaceEntity> saveAll(List<TripPlaceEntity> tripPlaceEntities) {
        return tripPlaceRepository.saveAll(tripPlaceEntities);
    }

    public void delete(TripPlaceEntity tripPlaceEntity) {
        tripPlaceRepository.delete(tripPlaceEntity);
    }

    public void deleteAllByTrip(TripEntity tripEntity) {
        tripPlaceRepository.deleteAllByTrip(tripEntity);
    }

    public void updateDetails(TripPlaceEntity tripPlaceEntity, int dayIndex, int visitOrder, String memo) {
        tripPlaceEntity.updateDetails(dayIndex, visitOrder, memo);
        tripPlaceRepository.save(tripPlaceEntity);
    }
}
