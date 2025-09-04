package com.chocobi.leafy.fcm.controller;

import com.chocobi.leafy.fcm.dto.FCMTokenDTO;
import com.chocobi.leafy.fcm.service.UserDeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    public UserDeviceController(UserDeviceService userDeviceService) {
        this.userDeviceService = userDeviceService;
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<Void> registerToken(@RequestBody FCMTokenDTO request,
                                              @AuthenticationPrincipal Long userId) { // 타입을 Long으로 변경
        // 서비스 레이어에 FCM 토큰과 userId를 함께 전달
        userDeviceService.registerToken(userId, request.getFcmToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/fcm-token-delete")
    public ResponseEntity<Void> unregisterToken(@RequestBody FCMTokenDTO request,
                                                @AuthenticationPrincipal Long userId) {
        userDeviceService.unregisterToken(userId, request.getFcmToken());
        return ResponseEntity.ok().build();
    }
}