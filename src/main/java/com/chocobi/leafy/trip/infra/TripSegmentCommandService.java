package com.chocobi.leafy.trip.infra;

import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripRouteOptionEntity;
import com.chocobi.leafy.trip.infra.entity.TripSegmentEntity;
import com.chocobi.leafy.trip.infra.repository.TripSegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TripSegmentCommandService {
    private final TripSegmentRepository tripSegmentRepository;

    public TripSegmentEntity save(TripSegmentEntity tripSegmentEntity) {
        return tripSegmentRepository.save(tripSegmentEntity);
    }

    public List<TripSegmentEntity> saveAll(List<TripSegmentEntity> tripSegmentEntities) {
        return tripSegmentRepository.saveAll(tripSegmentEntities);
    }

    public void delete(TripSegmentEntity tripSegmentEntity) {
        tripSegmentRepository.delete(tripSegmentEntity);
    }

    public void deleteAllByTrip(TripEntity tripEntity) {
        tripSegmentRepository.deleteAllByRouteOption_Trip_Id(tripEntity.getId());
    }

    public void deleteAllByRouteOption(TripRouteOptionEntity routeOption) {
        tripSegmentRepository.deleteAllByRouteOption(routeOption);
    }
}
