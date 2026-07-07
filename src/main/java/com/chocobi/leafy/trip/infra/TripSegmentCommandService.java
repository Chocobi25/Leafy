package com.chocobi.leafy.trip.infra;

import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.repository.TripSegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TripSegmentCommandService {
    private final TripSegmentRepository tripSegmentRepository;

    public void deleteAll(TripEntity tripEntity) {
        tripSegmentRepository.deleteAllByRouteOption_Trip_Id(tripEntity.getId());
    }
}
