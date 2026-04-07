package com.chocobi.leafy.post.infra.repository;

import com.chocobi.leafy.post.infra.entity.PostEntity;
import com.chocobi.leafy.post.infra.entity.PostLikeEntity;
import com.chocobi.leafy.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLikeEntity, Long> {
    boolean existsByUserAndPost(User user, PostEntity post);
    Optional<PostLikeEntity> findByUserAndPost(User user, PostEntity post);
}