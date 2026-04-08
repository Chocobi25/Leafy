package com.chocobi.leafy.post.presentation;

import com.chocobi.leafy.global.exception.ErrorResponse;
import com.chocobi.leafy.global.response.PageResponse;
import com.chocobi.leafy.global.response.SuccessResponse;
import com.chocobi.leafy.post.dto.request.PostCreateRequest;
import com.chocobi.leafy.post.dto.request.PostPageRequest;
import com.chocobi.leafy.post.dto.request.PostUpdateRequest;
import com.chocobi.leafy.post.dto.response.PostDetailResponse;
import com.chocobi.leafy.post.dto.response.PostLikeResponse;
import com.chocobi.leafy.post.dto.response.PostListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "포스트 API", description = "게시글 CRUD 등")
public interface PostDocs {

    @Operation(summary = "게시글 목록 조회")
    @ApiResponse(responseCode = "200",
            content = @Content(
                    schema = @Schema(implementation = PageResponse.class),
                    examples = @ExampleObject(value = """
                            {
                                "code": "SUCCESS",
                                "message": "요청이 성공했습니다.",
                                "data": [
                                    {
                                        "id": 1,
                                        "title": "리피 어쩌고저쩌고",
                                        "likes": 10,
                                        "viewCount": 100,
                                        "userId": 1,
                                        "userNickname": "초코비",
                                        "createdAt": "2026-01-01T00:00:00"
                                    }
                                ]
                            }
                            """)
            ))
    ResponseEntity<SuccessResponse<PageResponse<PostListResponse>>> getPosts(
            @ModelAttribute PostPageRequest request
    );

    @Operation(summary = "게시글 상세 조회")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = PostDetailResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "code": "SUCCESS",
                                        "message": "요청이 성공했습니다.",
                                        "data": {
                                            "id": 1,
                                            "title": "제목",
                                            "content": "내용",
                                            "likes": 10,
                                            "viewCount": 100,
                                            "userId": 1,
                                            "userNickname": "닉네임",
                                            "comments": [
                                                {
                                                    "id": 1,
                                                    "parentId": null,
                                                    "content": "댓글 내용",
                                                    "userId": 2,
                                                    "userNickname": "댓글작성자",
                                                    "createdAt": "2024-01-01T00:00:00"
                                                }
                                            ],
                                            "createdAt": "2024-01-01T00:00:00",
                                            "updatedAt": "2024-01-01T00:00:00"
                                        }
                                    }
                                    """)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "status": 404,
                                        "code": "POST_NOT_FOUND",
                                        "message": "존재하지 않는 게시글입니다.",
                                        "timestamp": "2024-01-01T00:00:00"
                                    }
                                    """)
                    ))
    })
    ResponseEntity<SuccessResponse<PostDetailResponse>> getPost(
            @PathVariable Long postId
    );

    @Operation(summary = "게시글 생성")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = PostDetailResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "code": "SUCCESS",
                                        "message": "요청이 성공했습니다.",
                                        "data": {
                                            "id": 1,
                                            "title": "제목",
                                            "content": "내용",
                                            "likes": 0,
                                            "viewCount": 0,
                                            "userId": 1,
                                            "userNickname": "닉네임",
                                            "comments": [],
                                            "createdAt": "2024-01-01T00:00:00",
                                            "updatedAt": "2024-01-01T00:00:00"
                                        }
                                    }
                                    """)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "status": 404,
                                        "code": "ENTITY_NOT_FOUND",
                                        "message": "존재하지 않는 리소스입니다.",
                                        "timestamp": "2024-01-01T00:00:00"
                                    }
                                    """)
                    ))
    })
    ResponseEntity<SuccessResponse<PostDetailResponse>> createPost(
            @RequestBody PostCreateRequest request
    );

    @Operation(summary = "게시글 수정")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = PostDetailResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "code": "SUCCESS",
                                        "message": "요청이 성공했습니다.",
                                        "data": {
                                            "id": 1,
                                            "title": "리피 어쩌고저쩌고",
                                            "content": "아웅~~어쩌고저쩌고내용길게길게",
                                            "likes": 10,
                                            "viewCount": 100,
                                            "userId": 1,
                                            "userNickname": "초코비",
                                            "comments" = [
                                                {
                                                    "id": 1,
                                                    "parentId": null,
                                                    "content" : "댓글어쩌고저쩌고"
                                                    "userId" : 122
                                                    "userNickname" : "리핑"
                                                    "createdAt": "2024-01-01T00:00:00"
                                                }
                                            ],
                                            "createdAt": "2024-01-01T00:00:00",
                                            "updatedAt": "2024-01-02T00:00:00"
                                        }
                                    }
                                    """)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "status": 404,
                                        "code": "POST_NOT_FOUND",
                                        "message": "존재하지 않는 게시글입니다.",
                                        "timestamp": "2024-01-01T00:00:00"
                                    }
                                    """)
                    ))
    })
    ResponseEntity<SuccessResponse<PostDetailResponse>> updatePost(
            @RequestBody PostUpdateRequest request
    );

    @Operation(summary = "게시글 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "404",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "status": 404,
                                        "code": "POST_NOT_FOUND",
                                        "message": "존재하지 않는 게시글입니다.",
                                        "timestamp": "2024-01-01T00:00:00"
                                    }
                                    """)
                    ))
    })
    ResponseEntity<Void> deletePost(@PathVariable Long postId);

    @Operation(summary = "게시글 좋아요 토글")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = PostLikeResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "code": "SUCCESS",
                                        "message": "요청이 성공했습니다.",
                                        "data": {
                                            "id": 1,
                                            "likes": 11,
                                            "liked": true
                                        }
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "404",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "status": 404,
                                        "code": "POST_NOT_FOUND",
                                        "message": "존재하지 않는 게시글입니다.",
                                        "timestamp": "2024-01-01T00:00:00"
                                    }
                                    """)
                    ))
    })
    ResponseEntity<SuccessResponse<PostLikeResponse>> toggleLike(
            @PathVariable Long postId,
            @PathVariable Long userId
    );
}