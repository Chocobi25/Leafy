package com.chocobi.leafy.post.infra.repository;

import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.post.infra.entity.PostEntity;
import com.chocobi.leafy.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findByUser(User user);
    List<PostEntity> findByUser(User user, Pageable pageable);
    //List<PostEntity> findByPlace(Place place);
}
