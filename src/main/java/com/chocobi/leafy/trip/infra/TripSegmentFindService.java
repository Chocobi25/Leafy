package com.chocobi.leafy.trip.infra;

import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.trip.infra.entity.TripSegmentEntity;
import com.chocobi.leafy.trip.infra.repository.TripSegmentRepository;
import com.chocobi.leafy.trip.vo.TripError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripSegmentFindService {
    private final TripSegmentRepository tripSegmentRepository;

    public TripSegmentEntity findTripSegment(Long tripSegmentId) {
        return tripSegmentRepository.findById(tripSegmentId)
                .orElseThrow(() -> new CustomException(TripError.TRIP_SEGMENT_NOT_FOUND));
    }

    public List<TripSegmentEntity> findTripSegmentsByTripId(Long tripId) {
        return tripSegmentRepository.findByTrip_Id(tripId);
    }
}
