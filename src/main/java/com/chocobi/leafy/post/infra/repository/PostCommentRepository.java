package com.chocobi.leafy.post.infra.repository;

import com.chocobi.leafy.post.infra.entity.PostCommentEntity;
import com.chocobi.leafy.post.infra.entity.PostEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostCommentRepository extends JpaRepository<PostCommentEntity, Long> {
    List<PostCommentEntity> findByPost(PostEntity post);
}
