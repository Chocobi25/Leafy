package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.fcm.service.FCMService;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import com.chocobi.leafy.user.infra.service.UserService;
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
     * 여행 생성 알림 🌱
     */
    @Transactional
    public void notifyTripCreated(Long userId, Long tripId) throws FirebaseMessagingException {
        UserEntity user = userService.findById(userId);  // TODO: 로직 동작 확인
        TripEntity trip = tripService.getTripById(tripId);

        Map<String, String> data = Map.of(
                "tripId", trip.getId().toString(),
                "url", "/trip/" + trip.getId()
        );

        fcmService.sendNotification(
                user,
                "🌿 여행이 준비되었어요!",
                "‘" + trip.getTitle() + "’ 여행이 성공적으로 생성되었습니다.",
                data
        );
    }

    /**
     * 여행 출발 알림 ✈️
     */
    @Transactional
    public void notifyTripStart(UserEntity user, TripEntity trip) throws FirebaseMessagingException {
        Map<String, String> data = Map.of(
                "tripId", trip.getId().toString(),
                "url", "/trip/" + trip.getId()
        );

        fcmService.sendNotification(
                user,
                "✈️ 출발 준비 완료!",
                "오늘은 ‘" + trip.getTitle() + "’ 여행이 시작되는 날이에요. 즐거운 여행 되세요!",
                data
        );
    }

    /**
     * 위치 인증 요청 📍
     */
    @Transactional
    public void requestLocationCheck(UserEntity user, TripEntity trip) throws FirebaseMessagingException {
        Map<String, String> data = Map.of(
                "tripId", trip.getId().toString(),
                "url", "/trip/" + trip.getId() + "/certify"
        );

        fcmService.sendNotification(
                user,
                "📍 위치 인증이 필요해요",
                "‘" + trip.getTitle() + "’ 여행의 인증을 위해 현재 위치를 확인해주세요.",
                data
        );
    }

    /**
     * 여행 인증 완료 🎉
     */
    @Transactional
    public void certifyTrip(UserEntity user, TripEntity trip) throws FirebaseMessagingException {
        Map<String, String> data = Map.of(
                "tripId", trip.getId().toString(),
                "url", "/trip/" + trip.getId()
        );

        fcmService.sendNotification(
                user,
                "🎉 여행 인증 완료!",
                "‘" + trip.getTitle() + "’ 여행 인증이 완료되었습니다. 다음 여정을 향해 나아가볼까요? 🌿",
                data
        );
    }
}