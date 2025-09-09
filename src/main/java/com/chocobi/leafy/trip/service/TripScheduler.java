package com.chocobi.leafy.trip.service;

import com.chocobi.leafy.fcm.service.FCMService;
import com.chocobi.leafy.fcm.service.UserDeviceService;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripStatus;
import com.chocobi.leafy.trip.repository.TripRepository;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserDeviceService userDeviceService;
    private final FCMService fcmService;

    /**
     * 7일 이상 'CREATING' 상태인 임시 여행 계획 매일 자정에 삭제하기
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteOldCreatingTrips() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        List<Trip> oldTrips = tripRepository.findByStatusAndCreatedAtBefore(TripStatus.CREATING, threshold);

        if (!oldTrips.isEmpty()) {
            tripRepository.deleteAll(oldTrips);
            log.info("Deleted {} old creating trips.", oldTrips.size());
        }
    }

    /**
     * 여행 종료 다음 날에 완료 축하 및 포스팅 관련 메세지 전송하기
     */
    @Scheduled(cron = "0 0 12 * * *")
    @Transactional(readOnly = true)
    public void sendTripCompletionMessages() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Trip> completedTrips = tripRepository.findByStatusAndEndDate(TripStatus.IN_PROGRESS, yesterday);

        if (completedTrips.isEmpty()) {
            log.info("어제 종료된 여행이 없습니다. 완료 알림을 보내지 않습니다.");
            return;
        }

        completedTrips.forEach(trip -> {
            try {
                String fcmToken = userDeviceService.getFcmTokenByUserId(trip.getUser());
                if (fcmToken != null) {
                    String title = "✅ 여행 완료 축하 메시지!";
                    String body = "멋진 여행이었어요! 여행 탄소량을 확인하고, 추억을 포스팅해 보세요.";
                    Map<String, String> data = Map.of(
                            "tripId", String.valueOf(trip.getId()),
                            "type", "trip-completion"
                    );
                    fcmService.sendDataNotification(fcmToken, title, body, data);
                    log.info("User {}에게 여행 완료 알림 전송 완료 (Trip ID: {})", trip.getUser().getKakaoId(), trip.getId());
                }
            } catch (FirebaseMessagingException | IllegalArgumentException e) {
                log.error("여행 완료 알림 전송 중 오류 발생 (Trip ID: {}): {}", trip.getId(), e.getMessage());
            }
        });
    }
}