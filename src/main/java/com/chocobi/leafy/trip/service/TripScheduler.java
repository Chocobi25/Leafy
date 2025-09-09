package com.chocobi.leafy.trip.service;

import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripStatus;
import com.chocobi.leafy.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class TripScheduler {
    private final TripRepository tripRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldCreatingTrips() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        List<Trip> oldTrips = tripRepository.findByStatusAndCreatedAtBefore(TripStatus.CREATING, threshold);

        if (!oldTrips.isEmpty()) {
            tripRepository.deleteAll(oldTrips);
            log.info("Deleted {} old creating trips.", oldTrips.size());
        }
    }
}
