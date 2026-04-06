package com.chocobi.leafy.post.application;

import com.chocobi.leafy.place.service.PlaceService;
import com.chocobi.leafy.post.dto.PostRequest;
import com.chocobi.leafy.post.dto.PostResponse;
import com.chocobi.leafy.post.infra.entity.PostEntity;
import com.chocobi.leafy.post.infra.entity.PostLikeEntity;
import com.chocobi.leafy.post.infra.repository.PostLikeRepository;
import com.chocobi.leafy.post.infra.repository.PostRepository;
import com.chocobi.leafy.user.entity.User;
import com.chocobi.leafy.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostLikeRepository userPostLikeRepository;
    private final PlaceService placeService;
    private final UserService userService;

    @Transactional
    public PostResponse createPost(PostRequest request) {
        PostEntity post = PostEntity.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(userService.findById(request.getUserId()))  // TODO: 로직 동작 확인
                .placeId(request.getPlaceId())
                .build();

        return PostResponse.fromEntity(postRepository.save(post));
    }

    @Transactional
    public PostResponse updatePost(PostRequest request) {
        PostEntity post = getPostById(request.getId());

        post.update(request.getTitle(), request.getContent(), request.getPlaceId());

        return PostResponse.fromEntity(postRepository.save(post));
    }

    public PostEntity getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글이 없습니다."));
    }

    @Transactional
    public void delete(Long postId) {
        postRepository.deleteById(postId);
    }

    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostResponse::fromEntity)
                .toList();
    }

    public List<PostResponse> getPostByUser(Long userId) {
        return postRepository.findByUser(userService.findById(userId)).stream()  // TODO: 로직 동작 확인
                .map(PostResponse::fromEntity)
                .toList();
    }

    /*public List<PostResponse> getPostByPlace(Long placeId){
        return postRepository.findByPlace(placeService.getPlaceById(placeId)).stream()
                .map(PostResponse::fromEntity)
                .toList();
    }*/

    @Transactional
    public PostResponse toggleLike(Long postId, Long userId) {
        PostEntity post = getPostById(postId);
        User user = userService.findById(userId);  // TODO: 로직 동작 확인

        boolean isCurrentlyLiked = userPostLikeRepository.existsByUserAndPost(user, post);

        if (isCurrentlyLiked) {
            // 좋아요 취소
            userPostLikeRepository.findByUserAndPost(user, post)
                    .ifPresent(userPostLikeRepository::delete);
            post.decrementLikes();
        } else {
            // 좋아요 추가
            PostLikeEntity userPostLike = PostLikeEntity.builder()
                    .user(user)
                    .post(post)
                    .build();
            userPostLikeRepository.save(userPostLike);
            post.incrementLikes();
        }

        return PostResponse.fromEntity(postRepository.save(post));
    }

    public List<Long> getUserLikedPostIds(Long userId) {
        User user = userService.findById(userId);  // TODO: 로직 동작 확인
        return userPostLikeRepository.findPostIdsByUser(user);
    }
}
