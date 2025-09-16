package com.chocobi.leafy.trip.service;

import com.chocobi.leafy.fcm.service.FCMService;
import com.chocobi.leafy.trip.dto.*;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripStatus;
import com.chocobi.leafy.trip.repository.TripRepository;
import com.chocobi.leafy.user.entity.User;
import com.chocobi.leafy.user.service.UserService;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
@AllArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final UserService userService;
    private final FCMService fcmService;

    public Long createTrip(TripRequest tripRequest, Long kakaoId) {
        Trip trip = Trip.builder()
                .user(userService.findByKakaoId(kakaoId))
                .title(tripRequest.getTitle())
                .start_date(tripRequest.getStart_date())
                .end_date(tripRequest.getEnd_date())
                .build();
        tripRepository.save(trip);
        return trip.getId();
    }

    public void deleteTrip(Long tripId) {
        tripRepository.deleteById(tripId);
    }

    public void updateTrip(Long tripId, TripRequest tripRequest) {
        Trip trip = getTripById(tripId);

        trip.update(tripRequest.getTitle(), tripRequest.getStart_date(), tripRequest.getEnd_date());
        tripRepository.save(trip);
    }

    public Trip getTripById(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 여행입니다."));
    }

    @Transactional
    public void requestLocationCheck(Long userId, Trip trip) throws FirebaseMessagingException {
        User user = userService.findByKakaoId(userId);

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
        tripRepository.save(trip);

        // 인증 완료 알림
        fcmService.sendNotification(
                user,
                "위치 인증 완료!",
                trip.getTitle() + " 여행의 위치 인증이 완료되었습니다.",
                Map.of("tripId", trip.getId().toString())
        );
    }
}
