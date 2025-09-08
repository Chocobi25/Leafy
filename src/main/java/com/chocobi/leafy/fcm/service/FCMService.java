package com.chocobi.leafy.fcm.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    // 데이터 페이로드를 포함한 알림 전송
    public void sendDataNotification(String fcmToken, String title, String body, Map<String, String> data) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(createNotification(title, body))
                .putAllData(data) // 데이터 페이로드를 메시지에 추가
                .build();

        FirebaseMessaging.getInstance().send(message);
    }

    // 페이로드 X
    public void sendSimpleNotification(String fcmToken, String title, String body) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(createNotification(title, body))
                .build();

        FirebaseMessaging.getInstance().send(message);
    }

    private Notification createNotification(String title, String body) {
        return Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();
    }
}