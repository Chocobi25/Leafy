package com.chocobi.leafy.post.infra;

import com.chocobi.leafy.post.infra.entity.PostEntity;
import com.chocobi.leafy.post.infra.entity.PostLikeEntity;
import com.chocobi.leafy.post.infra.repository.PostLikeRepository;
import com.chocobi.leafy.user.entity.User;
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

    public boolean exists(User user, PostEntity post) {
        return postLikeRepository.existsByUserAndPost(user, post);
    }

    public Optional<PostLikeEntity> findPost(User user, PostEntity post) {
        return postLikeRepository.findByUserAndPost(user, post);
    }
}
