package com.chocobi.leafy.post.repository;

import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.post.entity.Post;
import com.chocobi.leafy.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUser(User user);
    List<Post> findByUser(User user, Pageable pageable);
    List<Post> findByPlace(Place place);
}
