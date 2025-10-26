package com.chocobi.leafy.post.repository;

import com.chocobi.leafy.post.entity.Post;
import com.chocobi.leafy.post.entity.UserPostLike;
import com.chocobi.leafy.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPostLikeRepository extends JpaRepository<UserPostLike, Long> {

    // 특정 사용자와 포스트에 대한 좋아요 존재 확인
    boolean existsByUserAndPost(User user, Post post);

    // 특정 사용자와 포스트에 대한 좋아요 찾기
    Optional<UserPostLike> findByUserAndPost(User user, Post post);

    // 특정 사용자가 좋아요한 포스트 ID 목록 조회
    @Query("SELECT upl.post.id FROM UserPostLike upl WHERE upl.user = :user")
    List<Long> findPostIdsByUser(@Param("user") User user);

    // 특정 포스트의 총 좋아요 수 조회
    long countByPost(Post post);
}