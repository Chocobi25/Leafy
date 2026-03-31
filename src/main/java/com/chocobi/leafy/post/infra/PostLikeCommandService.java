package com.chocobi.leafy.post.infra;

import com.chocobi.leafy.post.infra.entity.PostLikeEntity;
import com.chocobi.leafy.post.infra.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostLikeCommandService {
    private final PostLikeRepository postLikeRepository;

    public void save(PostLikeEntity postLikeEntity) {
        postLikeRepository.save(postLikeEntity);
    }

    public void delete(PostLikeEntity postLikeEntity) {
        postLikeRepository.delete(postLikeEntity);
    }
}
