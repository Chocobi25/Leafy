package com.chocobi.leafy.user.repository;

import com.chocobi.leafy.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKakaoId(Long kakaoId); // 유저가 있을 수도 있고, 없을 수도 있음
}
