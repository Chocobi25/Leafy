package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripStatus;
import com.chocobi.leafy.trip.infra.repository.TripRepository;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class TripScheduler {
    private final TripRepository tripRepository;
    private final TripMessageService tripMessageService;
    private final TripService tripService;

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul") // 매일 자정
    @Transactional
    public void processTripsDaily() throws FirebaseMessagingException {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);

        // 1. 오래된 Creating Trip 삭제
        List<TripEntity> oldTrips = tripRepository.findByStatusAndCreatedAtBefore(TripStatus.CREATING, threshold);
        if (!oldTrips.isEmpty()) {
            tripRepository.deleteAll(oldTrips);
            log.info("Deleted {} old creating trips.", oldTrips.size());
        }

        // 2. 오늘 출발 Ready Trip -> InProgress로 상태 변경
        LocalDate today = LocalDate.now();
        List<TripEntity> readyTrips = tripRepository.findAllByStartDateAndStatus(today, TripStatus.READY);

        for (TripEntity trip : readyTrips) {
            tripService.changeTripStatus(trip.getId(), TripStatus.IN_PROGRESS);
        }
    }

    @Scheduled(cron = "0 0 10 * * *") // 매일 10시 실행
    public void sendTripStartNotification() throws FirebaseMessagingException {
        LocalDate today = LocalDate.now();
        List<TripEntity> tripsStartingToday = tripRepository.findAllByStartDateAndStatus(today, TripStatus.IN_PROGRESS);

        for (TripEntity trip : tripsStartingToday) {
            UserEntity participant = trip.getUser();
            tripMessageService.notifyTripStart(participant, trip);
        }
    }

    /**
     * startDate 17:00 PM: 위치 인증 알림 전송
     */
    @Scheduled(cron = "0 0 17 * * *", zone = "Asia/Seoul")
    public void sendCertifyNotification() throws FirebaseMessagingException {
        LocalDate today = LocalDate.now();
        List<TripEntity> tripsStartingToday = tripRepository.findAllByStartDateAndStatus(today, TripStatus.IN_PROGRESS);

        for (TripEntity trip : tripsStartingToday) {
            UserEntity participant = trip.getUser();
            tripMessageService.requestLocationCheck(participant, trip);
        }
    }
}
