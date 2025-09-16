package com.chocobi.leafy.trip.service;

import com.chocobi.leafy.fcm.service.FCMService;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripStatus;
import com.chocobi.leafy.trip.repository.TripRepository;
import com.chocobi.leafy.user.entity.User;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class TripScheduler {
    private final TripRepository tripRepository;
    private final FCMService fcmService;

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldCreatingTrips() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        List<Trip> oldTrips = tripRepository.findByStatusAndCreatedAtBefore(TripStatus.CREATING, threshold);

        if (!oldTrips.isEmpty()) {
            tripRepository.deleteAll(oldTrips);
            log.info("Deleted {} old creating trips.", oldTrips.size());
        }
    }

    @Scheduled(cron = "0 0 10 * * *") // 매일 10시 실행
    public void sendTripStartNotification() throws FirebaseMessagingException {
        LocalDate today = LocalDate.now();
        List<Trip> tripsStartingToday = tripRepository.findAllByStartDateAndStatus(today, TripStatus.IN_PROGRESS);

        for (Trip trip : tripsStartingToday) {
            User participant = trip.getUser();
            fcmService.sendNotification(
                    participant,
                    "여행 출발!",
                    trip.getTitle() + " 여행이 오늘 출발합니다!",
                    Map.of("tripId", trip.getId().toString())
            );
        }
    }
}
