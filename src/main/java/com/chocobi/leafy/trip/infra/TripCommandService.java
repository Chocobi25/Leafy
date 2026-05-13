package com.chocobi.leafy.trip.infra;

import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripStatus;
import com.chocobi.leafy.trip.infra.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TripCommandService {
    private final TripRepository tripRepository;

    public TripEntity save(TripEntity tripEntity) {
        return tripRepository.save(tripEntity);
    }

    public void delete(TripEntity tripEntity) {
        tripRepository.delete(tripEntity);
    }

    public void deleteAll(List<TripEntity> tripEntities) {
        tripRepository.deleteAll(tripEntities);
    }

    public void changeStatus(TripEntity tripEntity, TripStatus tripStatus) {
        tripEntity.editStatus(tripStatus);
        tripRepository.save(tripEntity);
    }

    public void updateInfo(TripEntity tripEntity, String title, LocalDate startDate, LocalDate endDate) {
        tripEntity.update(title, startDate, endDate);
        tripRepository.save(tripEntity);
    }
}
