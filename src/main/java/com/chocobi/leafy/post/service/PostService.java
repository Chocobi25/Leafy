package com.chocobi.leafy.post.service;

import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.service.PlaceService;
import com.chocobi.leafy.post.dto.PostRequest;
import com.chocobi.leafy.post.dto.PostResponse;
import com.chocobi.leafy.post.entity.Post;
import com.chocobi.leafy.post.entity.UserPostLike;
import com.chocobi.leafy.post.repository.PostRepository;
import com.chocobi.leafy.post.repository.UserPostLikeRepository;
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
    private final UserPostLikeRepository userPostLikeRepository;
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
                .likes(0)
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
    public PostResponse toggleLike(Long postId, Long userId) {
        Post post = getPostById(postId);
        User user = userService.findByKakaoId(userId);

        boolean isCurrentlyLiked = userPostLikeRepository.existsByUserAndPost(user, post);

        if (isCurrentlyLiked) {
            // 좋아요 취소
            userPostLikeRepository.findByUserAndPost(user, post)
                .ifPresent(userPostLikeRepository::delete);
            post.decrementLikes();
        } else {
            // 좋아요 추가
            UserPostLike userPostLike = UserPostLike.builder()
                .user(user)
                .post(post)
                .build();
            userPostLikeRepository.save(userPostLike);
            post.incrementLikes();
        }

        return PostResponse.fromEntity(postRepository.save(post));
    }

    public List<Long> getUserLikedPostIds(Long userId) {
        User user = userService.findByKakaoId(userId);
        return userPostLikeRepository.findPostIdsByUser(user);
    }
}
