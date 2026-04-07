package com.chocobi.leafy.fcm.service;

import com.chocobi.leafy.fcm.entity.UserDevice;
import com.chocobi.leafy.fcm.repository.UserDeviceRepository;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import com.chocobi.leafy.user.infra.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDeviceService {
    private final UserDeviceRepository userDeviceRepository;
    private final UserService userService;

    @Transactional
    public void registerToken(Long userId, String fcmToken) {
        // 1. 해당 사용자가 존재하는지 확인
        UserEntity userEntity = userService.findById(userId); // TODO: 로직 동작 확인

        // 2. 전달받은 토큰이 이미 DB에 등록되어 있는지 확인
        userDeviceRepository.findByFcmToken(fcmToken)
                .ifPresentOrElse(
                        // 2-1. 토큰이 이미 존재하는 경우
                        device -> {
                            if (!device.getUserEntity().getId().equals(userEntity.getId())) { // TODO: 로직 동작 확인
                                device.updateUser(userEntity);
                                log.info("FCM 토큰 {}의 사용자 정보가 ID {}로 업데이트되었습니다.", fcmToken, userId);
                            } else {
                                log.info("FCM 토큰 {}는 이미 ID {}에 등록되어 있습니다. 변경 없음.", fcmToken, userId);
                            }
                        },
                        // 2-2. 토큰이 존재하지 않는 새로운 토큰인 경우
                        () -> {
                            UserDevice newDevice = new UserDevice(fcmToken, userEntity);
                            userDeviceRepository.save(newDevice);
                            log.info("새로운 FCM 토큰이 사용자 ID {}에 성공적으로 등록되었습니다.", userId);
                        }
                );
    }

    @Transactional
    public void unregisterToken(Long userId, String fcmToken) {
        userDeviceRepository.findByUserAndFcmToken(userService.findById(userId), fcmToken)  // TODO: 로직 동작 확인
                .ifPresent(userDeviceRepository::delete);
        log.info("사용자 ID {}의 FCM 토큰 {}이 성공적으로 삭제되었습니다.", userId, fcmToken);
    }

    public String getFcmTokenByUserId(UserEntity userEntity) {
        return userDeviceRepository.findByUser(userEntity)
                .map(UserDevice::getFcmToken)
                .orElse(null);
    }
}