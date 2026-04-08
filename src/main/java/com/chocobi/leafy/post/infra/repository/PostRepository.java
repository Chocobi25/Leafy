package com.chocobi.leafy.post.infra.repository;

import com.chocobi.leafy.post.infra.entity.PostEntity;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findByUser(UserEntity user);
    List<PostEntity> findByUser(UserEntity user, Pageable pageable);
    //List<PostEntity> findByPlace(Place place);
}
