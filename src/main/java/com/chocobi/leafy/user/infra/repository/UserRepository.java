package com.chocobi.leafy.user.infra.repository;

import com.chocobi.leafy.user.infra.entity.UserEntity;
import com.chocobi.leafy.user.infra.entity.enums.Provider;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Modifying
    @Transactional
    @Query("update UserEntity u set u.totalCarbonSaved = u.totalCarbonSaved + ?2 where u.id = ?1")
        // TODO: 로직 동작 확인
    void updateCarbonSaved(Long id, double carbonSaved);

    @Query(value = "select * from users where provider = :provider and provider_id = :providerId",
            nativeQuery = true)
    Optional<UserEntity> findByProviderAndProviderId(@Param("provider") String provider, @Param("providerId") String providerId);

    void deleteAllByDeletedAtBefore(LocalDateTime deletedAtBefore);
}
