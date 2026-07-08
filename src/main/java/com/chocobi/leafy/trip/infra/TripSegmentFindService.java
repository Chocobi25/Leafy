package com.chocobi.leafy.trip.infra;

import com.chocobi.leafy.trip.infra.entity.TripSegmentEntity;
import com.chocobi.leafy.trip.infra.repository.TripSegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripSegmentFindService {
    private final TripSegmentRepository tripSegmentRepository;

    public List<TripSegmentEntity> findConfirmedTripSegments(Long tripId) {
        return tripSegmentRepository.findConfirmedTripSegments(tripId);
    }
}
