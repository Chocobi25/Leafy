package com.chocobi.leafy.post.infra;

import com.chocobi.leafy.post.infra.entity.PostEntity;
import com.chocobi.leafy.post.infra.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostCommandService {
    private final PostRepository postRepository;

    public void save(PostEntity postEntity) {
        postRepository.save(postEntity);
    }

    public void delete(Long postId) {
        postRepository.deleteById(postId);
    }
}
