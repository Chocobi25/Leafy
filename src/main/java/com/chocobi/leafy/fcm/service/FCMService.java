package com.chocobi.leafy.fcm.service;

import com.chocobi.leafy.fcm.entity.UserDevice;
import com.chocobi.leafy.fcm.repository.UserDeviceRepository;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    private final UserDeviceRepository userDeviceRepository;

    /**
     * 특정 사용자에게 단일 알림 전송
     */
    @Transactional
    public void sendNotification(UserEntity user, String title, String body, Map<String, String> data) throws FirebaseMessagingException {
        // 1. 사용자 디바이스 토큰 가져오기 (단일)
        Optional<UserDevice> deviceOpt = userDeviceRepository.findByUser(user);
        if (deviceOpt.isEmpty()) {
            log.warn("❌ 사용자 {} 에 등록된 FCM 토큰이 없습니다.", user.getId());  // TODO: 로직 동작 확인
            return;
        }

        String fcmToken = deviceOpt.get().getFcmToken();

        // 2. 메시지 생성
        Message.Builder builder = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build());

        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        Message message = builder.build();

        // 3. 발송
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("✅ FCM 알림 전송 완료 - user={}, token={}, response={}", user.getId(), fcmToken, response);  // TODO: 로직 동작 확인
        } catch (FirebaseMessagingException e) {
            log.error("❌ FCM 전송 실패 - user={}, token={}, reason={}", user.getId(), fcmToken, e.getMessagingErrorCode(), e);  // TODO: 로직 동작 확인
            // 실패 토큰 삭제
            userDeviceRepository.findByFcmToken(fcmToken)
                    .ifPresent(userDeviceRepository::delete);
        }
    }

    /**
     * 단일 토큰 알림 (테스트용)
     */
    public void sendNotification(String fcmToken, String title, String body, Map<String, String> data) throws FirebaseMessagingException {
        Message.Builder builder = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build());

        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        String response = FirebaseMessaging.getInstance().send(builder.build());
        log.info("✅ 단일 토큰 알림 전송 성공 - token={}, response={}", fcmToken, response);
    }
}