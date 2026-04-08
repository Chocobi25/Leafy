package com.chocobi.leafy.fcm.repository;

import com.chocobi.leafy.fcm.entity.UserDevice;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    Optional<UserDevice> findByFcmToken(String fcmToken);
    Optional<UserDevice> findByUserAndFcmToken(UserEntity user, String fcmToken);
    Optional<UserDevice> findByUser(UserEntity user);
}
