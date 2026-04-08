package com.chocobi.leafy.post.infra;

import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.global.exception.ErrorCode;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.post.dto.request.PostPageRequest;
import com.chocobi.leafy.post.dto.response.PostDetailResponse;
import com.chocobi.leafy.post.dto.response.PostListResponse;
import com.chocobi.leafy.post.infra.entity.PostEntity;
import com.chocobi.leafy.post.infra.repository.PostRepository;
import com.chocobi.leafy.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostFindService {
    private final PostRepository postRepository;

    public PostEntity findPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    }

    public List<PostEntity> findPosts() {
        return postRepository.findAll().stream().toList();
    }

    public Page<PostEntity> findPosts(PostPageRequest request) {
        Pageable pageable = PageRequest.of(request.page(), request.size());
        return postRepository.findAll(pageable);
    }
}
