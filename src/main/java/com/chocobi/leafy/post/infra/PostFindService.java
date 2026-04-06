package com.chocobi.leafy.post.infra;

import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.global.exception.ErrorCode;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.post.dto.response.PostDetailResponse;
import com.chocobi.leafy.post.infra.entity.PostEntity;
import com.chocobi.leafy.post.infra.repository.PostRepository;
import com.chocobi.leafy.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostFindService {
    private final PostRepository postRepository;

    public PostEntity getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    }

    public List<PostDetailResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostDetailResponse::from)
                .toList();
    }

    public List<PostDetailResponse> getPostsByUser(User user) {
        return postRepository.findByUser(user).stream()
                .map(PostDetailResponse::from)
                .toList();
    }
}
