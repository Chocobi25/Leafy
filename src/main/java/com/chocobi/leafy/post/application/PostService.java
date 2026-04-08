package com.chocobi.leafy.post.application;

import com.chocobi.leafy.global.response.PageResponse;
import com.chocobi.leafy.post.dto.request.PostCreateRequest;
import com.chocobi.leafy.post.dto.request.PostPageRequest;
import com.chocobi.leafy.post.dto.request.PostUpdateRequest;
import com.chocobi.leafy.post.dto.response.PostCommentResponse;
import com.chocobi.leafy.post.dto.response.PostDetailResponse;
import com.chocobi.leafy.post.dto.response.PostLikeResponse;
import com.chocobi.leafy.post.dto.response.PostListResponse;
import com.chocobi.leafy.post.infra.PostCommandService;
import com.chocobi.leafy.post.infra.PostCommentFindService;
import com.chocobi.leafy.post.infra.PostFindService;
import com.chocobi.leafy.post.infra.PostLikeCommandService;
import com.chocobi.leafy.post.infra.PostLikeFindService;
import com.chocobi.leafy.post.infra.entity.PostEntity;
import com.chocobi.leafy.post.infra.entity.PostLikeEntity;
import com.chocobi.leafy.user.infra.entity.UserEntity;
import com.chocobi.leafy.user.infra.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostFindService postFindService;
    private final PostCommandService postCommandService;
    private final PostCommentFindService postCommentFindService;
    private final PostLikeCommandService postLikeCommandService;
    private final PostLikeFindService postLikeFindService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public PageResponse<PostListResponse> getPosts(PostPageRequest request) {
        Page<PostListResponse> page = postFindService.findPosts(request)
                .map(PostListResponse::from);
        return PageResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPost(Long id) {
        PostEntity post = postFindService.findPost(id);
        List<PostCommentResponse> comments = postCommentFindService.findComments(post);

        return PostDetailResponse.from(post, comments);
    }

    @Transactional
    public PostDetailResponse createPost(PostCreateRequest request) {
        PostEntity post = PostEntity.builder()
                .title(request.title())
                .content(request.content())
                .user(userService.findById(request.userId()))
                .placeId(request.placeId())
                .build();

        postCommandService.save(post);
        return PostDetailResponse.from(post, List.of());
    }

    @Transactional
    public PostDetailResponse updatePost(PostUpdateRequest request) {
        PostEntity post = postFindService.findPost(request.postId());
        post.update(request.title(), request.content(), request.placeId());

        List<PostCommentResponse> comments = postCommentFindService.findComments(post);

        return PostDetailResponse.from(post, comments);
    }


    @Transactional
    public void deletePost(Long postId) {
        postCommandService.delete(postId);
    }

    @Transactional
    public PostLikeResponse toggleLike(Long postId, Long userId) {
        PostEntity post = postFindService.findPost(postId);
        UserEntity user = userService.findById(userId);

        boolean isCurrentlyLiked = postLikeFindService.exists(user, post);

        if (isCurrentlyLiked) {
            postLikeFindService.findPost(user, post)
                    .ifPresent(postLikeCommandService::delete);
            post.decrementLikes();
        } else {
            postLikeCommandService.save(PostLikeEntity.builder()
                    .user(user)
                    .post(post)
                    .build());
            post.incrementLikes();
        }

        return PostLikeResponse.of(post, !isCurrentlyLiked);
    }
}
