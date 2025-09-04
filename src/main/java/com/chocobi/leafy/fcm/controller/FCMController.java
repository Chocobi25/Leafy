package com.chocobi.leafy.fcm.controller;

import com.chocobi.leafy.fcm.service.FCMService;
import com.chocobi.leafy.fcm.dto.FCMTokenDTO;
import com.chocobi.leafy.fcm.service.UserDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FCMController {
    private final FCMService fcmService;
    private final UserDeviceService userDeviceService;

    // 토큰 저장
    @PostMapping("/register-token")
    public ResponseEntity<Void> registerToken(
            @AuthenticationPrincipal Long userId,
            @RequestBody FCMTokenDTO fcmTokenDTO) {

        log.info("사용자 ID {}로부터 FCM 토큰 등록 요청 수신.", userId);
        userDeviceService.registerToken(userId, fcmTokenDTO.getFcmToken());

        return ResponseEntity.ok().build();
    }

    // 토큰 삭제
    @PostMapping("/unregister-token")
    public ResponseEntity<Void> unregisterToken(
            @AuthenticationPrincipal Long userId,
            @RequestBody FCMTokenDTO fcmTokenDTO) {

        log.info("사용자 ID {}로부터 FCM 토큰 삭제 요청 수신.", userId);
        userDeviceService.unregisterToken(userId, fcmTokenDTO.getFcmToken());

        return ResponseEntity.ok().build();
    }
}