package com.chocobi.leafy.trip.service;

import com.chocobi.leafy.fcm.service.FCMService;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.user.entity.User;
import com.chocobi.leafy.user.service.UserService;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TripMessageService {
    private final FCMService fcmService;
    private final TripService tripService;
    private final UserService userService;

    /**
     * 여행 생성 시 알림
     */
    @Transactional
    public void notifyTripCreated(Long userId, Long tripId) throws FirebaseMessagingException {
        User user = userService.findByKakaoId(userId);
        Trip trip = tripService.getTripById(tripId);

        Map<String, String> data = Map.of(
                "tripId", trip.getId().toString(),
                "url", "/trips/" + trip.getId() // 프론트 라우팅
        );

        fcmService.sendNotification(
                user,
                "여행 생성 완료!",
                trip.getTitle() + " 여행이 생성되었습니다.",
                data
        );
    }

    /**
     * 여행 출발 알림 (스케줄러 호출용)
     */
    @Transactional
    public void notifyTripStart(User user, Trip trip) throws FirebaseMessagingException {
        Map<String, String> data = Map.of(
                "tripId", trip.getId().toString(),
                "url", "/trips/" + trip.getId()
        );

        fcmService.sendNotification(
                user,
                "여행 출발!",
                trip.getTitle() + " 여행이 오늘 출발합니다!",
                data
        );
    }


    @Transactional
    public void requestLocationCheck(User user, Trip trip) throws FirebaseMessagingException {
        Map<String, String> data = Map.of(
                "tripId", trip.getId().toString(),
                "url", "/trips/certification" + trip.getId()  // 프론트 라우팅 경로
        );

        fcmService.sendNotification(
                user,
                "위치 인증 요청",
                trip.getTitle() + " 여행에서 위치 인증을 해주세요!",
                data
        );
    }

    @Transactional
    public void certifyTrip(Long id, Trip trip) throws FirebaseMessagingException {
        User user = userService.findByKakaoId(id);
        trip.certify(); // 인증 시간 업데이트
        tripService.saveTrip(trip);

        // 인증 완료 알림
        fcmService.sendNotification(
                user,
                "위치 인증 완료!",
                trip.getTitle() + " 여행의 위치 인증이 완료되었습니다.",
                Map.of("tripId", trip.getId().toString())
        );
    }
}
