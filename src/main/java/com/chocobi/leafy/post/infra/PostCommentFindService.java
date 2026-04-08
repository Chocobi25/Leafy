package com.chocobi.leafy.post.infra;

import com.chocobi.leafy.post.dto.response.PostCommentResponse;
import com.chocobi.leafy.post.infra.entity.PostEntity;
import com.chocobi.leafy.post.infra.repository.PostCommentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostCommentFindService {
    private final PostCommentRepository commentRepository;

    public List<PostCommentResponse> findComments(PostEntity post) {
        return commentRepository.findByPost(post)
                .stream()
                .map(PostCommentResponse::from)
                .toList();
    }
}
