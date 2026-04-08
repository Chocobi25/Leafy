package com.chocobi.leafy.post.infra;

import com.chocobi.leafy.post.infra.entity.PostEntity;
import com.chocobi.leafy.post.infra.entity.PostLikeEntity;
import com.chocobi.leafy.post.infra.repository.PostLikeRepository;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostLikeFindService {
    private final PostLikeRepository postLikeRepository;

    public boolean exists(UserEntity user, PostEntity post) {
        return postLikeRepository.existsByUserAndPost(user, post);
    }

    public Optional<PostLikeEntity> findPost(UserEntity user, PostEntity post) {
        return postLikeRepository.findByUserAndPost(user, post);
    }
}
