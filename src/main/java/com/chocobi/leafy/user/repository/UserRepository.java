package com.chocobi.leafy.user.repository;

import com.chocobi.leafy.user.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKakaoId(Long kakaoId); // 유저가 있을 수도 있고, 없을 수도 있음

    @Query("update User u set u.totalCarbonSaved = u.totalCarbonSaved + ?2 where u.kakaoId = ?1")
    void updateCarbonSaved(Long kakaoId, double carbonSaved);
}
