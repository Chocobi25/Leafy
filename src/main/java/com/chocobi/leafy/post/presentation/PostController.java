package com.chocobi.leafy.post.presentation;

import com.chocobi.leafy.global.response.SuccessResponse;
import com.chocobi.leafy.global.response.PageResponse;
import com.chocobi.leafy.post.application.PostService;
import com.chocobi.leafy.post.dto.request.PostCreateRequest;
import com.chocobi.leafy.post.dto.request.PostPageRequest;
import com.chocobi.leafy.post.dto.request.PostUpdateRequest;
import com.chocobi.leafy.post.dto.response.PostDetailResponse;
import com.chocobi.leafy.post.dto.response.PostLikeResponse;
import com.chocobi.leafy.post.dto.response.PostListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController implements PostDocs {
    private final PostService postService;

    @GetMapping("/v1/posts")
    public ResponseEntity<SuccessResponse<PageResponse<PostListResponse>>> getPosts(
            @ModelAttribute PostPageRequest request
    ) {
        return ResponseEntity.ok(SuccessResponse.of(postService.getPosts(request)));
    }

    @GetMapping("/v1/posts/{postId}")
    public ResponseEntity<SuccessResponse<PostDetailResponse>> getPost(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(postService.getPost(postId)));
    }

    @PostMapping("/v1/posts")
    public ResponseEntity<SuccessResponse<PostDetailResponse>> createPost(
            @Valid @RequestBody PostCreateRequest request
    ) {
        return ResponseEntity.ok(SuccessResponse.of(postService.createPost(request)));
    }


    @PutMapping("/v1/posts")
    public ResponseEntity<SuccessResponse<PostDetailResponse>> updatePost(
            @Valid @RequestBody PostUpdateRequest request
    ) {
        return ResponseEntity.ok(SuccessResponse.of(postService.updatePost(request)));
    }

    @DeleteMapping("/v1/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId
    ) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/v1/posts/{postId}/likes/{userId}")
    public ResponseEntity<SuccessResponse<PostLikeResponse>> toggleLike(
            @PathVariable Long postId,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(SuccessResponse.of(postService.toggleLike(postId, userId)));
    }
}
