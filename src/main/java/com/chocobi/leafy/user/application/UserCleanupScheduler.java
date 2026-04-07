package com.chocobi.leafy.user.application;

import com.chocobi.leafy.user.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    @Value("${app.user.withdrawal-recovery-days:30}")
    private int withdrawalRecoveryDays;

    @Scheduled(cron = "0 0 3 * * *")
    public void hardDeleteWithdrawnUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(withdrawalRecoveryDays);
        userRepository.deleteAllByDeletedAtBefore(threshold);
    }
}
