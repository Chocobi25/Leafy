package com.chocobi.leafy.post.service;

import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.service.PlaceService;
import com.chocobi.leafy.post.dto.PostRequest;
import com.chocobi.leafy.post.dto.PostResponse;
import com.chocobi.leafy.post.entity.Post;
import com.chocobi.leafy.post.repository.PostRepository;
import com.chocobi.leafy.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PlaceService placeService;
    private final UserService userService;

    @Transactional
    public PostResponse createPost(PostRequest request) {
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(userService.findByKakaoId(request.getUserId()))
                .place(placeService.getPlaceById(request.getPlaceId()))
                .rating(request.getRating())
                .build();

        return PostResponse.fromEntity(postRepository.save(post));
    }

    @Transactional
    public PostResponse updatePost(PostRequest request) {
        Post post = getPostById(request.getId());
        Place place = placeService.getPlaceById(request.getPlaceId());

        post.updatePost(request, place);

        return PostResponse.fromEntity(postRepository.save(post));
    }

    public Post getPostById(Long id) {
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
        return postRepository.findByUser(userService.findByKakaoId(userId)).stream()
                .map(PostResponse::fromEntity)
                .toList();
    }

    public List<PostResponse> getPostByPlace(Long placeId){
        return postRepository.findByPlace(placeService.getPlaceById(placeId)).stream()
                .map(PostResponse::fromEntity)
                .toList();
    }

    @Transactional
    public PostResponse toggleLike(Long postId, boolean isCurrentlyLiked) {
        Post post = getPostById(postId);
        if (isCurrentlyLiked) {
            // 현재 좋아요 상태면 취소 (-1)
            post.decrementLikes();
        } else {
            // 현재 좋아요 안한 상태면 추가 (+1)
            post.incrementLikes();
        }
        return PostResponse.fromEntity(postRepository.save(post));
    }
}
