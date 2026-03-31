package com.chocobi.leafy.post.infra;

import com.chocobi.leafy.post.infra.entity.PostCommentEntity;
import com.chocobi.leafy.post.infra.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostCommentCommandService {
    private final PostCommentRepository postCommentRepository;

    public void save(PostCommentEntity postCommentEntity) {
        postCommentRepository.save(postCommentEntity);
    }

    public void delete(PostCommentEntity postCommentEntity) {
        postCommentRepository.delete(postCommentEntity);
    }
}
