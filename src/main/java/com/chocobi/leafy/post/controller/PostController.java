package com.chocobi.leafy.post.controller;

import com.chocobi.leafy.post.dto.PostRequest;
import com.chocobi.leafy.post.dto.PostResponse;
import com.chocobi.leafy.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("/admin")
    public ResponseEntity<PostResponse> createPost(@RequestBody PostRequest request) {
        return ResponseEntity.ok(postService.createPost(request));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(@RequestBody PostRequest request) {
        return ResponseEntity.ok(postService.updatePost(request));
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/place/{placeId}")
    public ResponseEntity<List<PostResponse>> getPostsByPlace(@PathVariable Long placeId) {
        return ResponseEntity.ok(postService.getPostByPlace(placeId));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.delete(postId);
        return ResponseEntity.noContent().build();
    }
}
