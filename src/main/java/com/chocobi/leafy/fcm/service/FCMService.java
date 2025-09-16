package com.chocobi.leafy.fcm.service;

import com.chocobi.leafy.fcm.entity.UserDevice;
import com.chocobi.leafy.fcm.repository.UserDeviceRepository;
import com.chocobi.leafy.user.entity.User;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    private final UserDeviceRepository userDeviceRepository;

    /**
     * 특정 사용자에게 알림 전송 (멀티 디바이스 지원)
     */
    @Transactional
    public void sendNotification(User user, String title, String body, Map<String, String> data) throws FirebaseMessagingException {
        // 1. 해당 유저의 모든 디바이스 토큰 가져오기
        Optional<UserDevice> devices = userDeviceRepository.findByUser(user);
        if (devices.isEmpty()) {
            log.warn("사용자 {}에게 등록된 FCM 토큰이 없습니다.", user.getKakaoId());
            return;
        }

        List<String> tokens = devices.stream()
                .map(UserDevice::getFcmToken)
                .toList();

        // 2. 멀티캐스트 메시지 생성
        MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build());

        if (data != null && !data.isEmpty()) {
            messageBuilder.putAllData(data);
        }

        MulticastMessage message = messageBuilder.build();

        // 3. 발송
        BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
        log.info("알림 발송 완료. 성공: {}, 실패: {}", response.getSuccessCount(), response.getFailureCount());

        // 4. 실패 토큰 삭제
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            if (!responses.get(i).isSuccessful()) {
                String failedToken = tokens.get(i);
                log.warn("FCM 토큰 {} 발송 실패. DB에서 삭제합니다.", failedToken);
                userDeviceRepository.findByFcmToken(failedToken)
                        .ifPresent(userDeviceRepository::delete);
            }
        }
    }

    /**
     * 단일 토큰 알림 (기존 sendSimpleNotification / sendDataNotification 대체 가능)
     */
    public void sendNotification(String fcmToken, String title, String body, Map<String, String> data) throws FirebaseMessagingException {
        Message.Builder messageBuilder = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build());

        if (data != null && !data.isEmpty()) {
            messageBuilder.putAllData(data);
        }

        FirebaseMessaging.getInstance().send(messageBuilder.build());
    }
}