package com.chocobi.leafy.user.repository;

import com.chocobi.leafy.user.entity.User;
import com.chocobi.leafy.user.enums.Provider;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Modifying
    @Transactional
    @Query("update User u set u.totalCarbonSaved = u.totalCarbonSaved + ?2 where u.id = ?1")  // TODO: 로직 동작 확인
    void updateCarbonSaved(Long id, double carbonSaved);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}
