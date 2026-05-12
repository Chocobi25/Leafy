package com.chocobi.leafy.place.presentation;

import com.chocobi.leafy.global.exception.ErrorResponse;
import com.chocobi.leafy.global.response.PageResponse;
import com.chocobi.leafy.place.dto.request.AdminCreatePlaceRequest;
import com.chocobi.leafy.place.dto.request.AdminPlacePageRequest;
import com.chocobi.leafy.place.dto.request.AdminUpdateCustomPlaceRequest;
import com.chocobi.leafy.place.dto.request.AdminUpdateExternalPlaceRequest;
import com.chocobi.leafy.place.dto.response.AdminPlaceDetailResponse;
import com.chocobi.leafy.place.dto.response.AdminPlaceListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "어드민 장소 API", description = "어드민 장소 조회, 생성, 수정, 삭제 API")
public interface AdminPlaceDocs {

    @Operation(summary = "장소 목록 조회",
            description = "page는 1부터 시작합니다. 최신순으로 정렬되며, placeType은 EXTERNAL 또는 CUSTOM을 사용할 수 있습니다.")
    @ApiResponse(responseCode = "200",
            content = @Content(
                    schema = @Schema(implementation = PageResponse.class),
                    examples = @ExampleObject(value = """
                            {
                                "content": [
                                    {
                                        "id": 1,
                                        "title": "남산서울타워",
                                        "placeType": "EXTERNAL",
                                        "category": "자연",
                                        "region": "서울",
                                        "createdAt": "2026-01-01T00:00:00",
                                        "updatedAt": "2026-01-01T00:00:00"
                                    },
                                    {
                                        "id": 2,
                                        "title": "내가 추가한 장소",
                                        "placeType": "CUSTOM",
                                        "category": null,
                                        "region": null,
                                        "createdAt": "2026-01-01T00:00:00",
                                        "updatedAt": "2026-01-01T00:00:00"
                                    }
                                ],
                                "page": 1,
                                "size": 10,
                                "totalElements": 2,
                                "totalPages": 1
                            }
                            """)
            ))
    ResponseEntity<PageResponse<AdminPlaceListResponse>> getPlaces(
            @Valid @ModelAttribute AdminPlacePageRequest request
    );

    @Operation(summary = "장소 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = AdminPlaceDetailResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "id": 1,
                                        "title": "남산서울타워",
                                        "placeType": "EXTERNAL",
                                        "description": "서울의 대표적인 전망 명소입니다.",
                                        "categoryId": 1,
                                        "category": "자연",
                                        "regionId": 1,
                                        "region": "서울",
                                        "address": "서울 용산구 남산공원길 105",
                                        "latitude": 37.55117,
                                        "longitude": 126.9882,
                                        "tel": "02-1234-5678",
                                        "url": "https://example.com/place/1",
                                        "copyright": "한국관광공사",
                                        "createdAt": "2026-01-01T00:00:00",
                                        "updatedAt": "2026-01-01T00:00:00"
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "404",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "status": 404,
                                        "code": "PLACE_NOT_FOUND",
                                        "message": "존재하지 않는 장소입니다.",
                                        "timestamp": "2026-01-01T00:00:00"
                                    }
                                    """)
                    ))
    })
    ResponseEntity<AdminPlaceDetailResponse> getPlace(@PathVariable Long placeId);

    @Operation(summary = "장소 생성")
    @ApiResponse(responseCode = "200",
            content = @Content(
                    schema = @Schema(implementation = Long.class),
                    examples = @ExampleObject(value = "1")
            ))
    ResponseEntity<Long> createPlace(@Valid @RequestBody AdminCreatePlaceRequest request);

    @Operation(summary = "어드민 외부 API 장소 수정")
    @ApiResponse(responseCode = "204", description = "수정 성공")
    ResponseEntity<Void> updateExternalPlace(@Valid @RequestBody AdminUpdateExternalPlaceRequest request);

    @Operation(summary = "어드민 커스텀 장소 수정")
    @ApiResponse(responseCode = "204", description = "수정 성공")
    ResponseEntity<Void> updateCustomPlace(@Valid @RequestBody AdminUpdateCustomPlaceRequest request);

    @Operation(summary = "장소 삭제")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    ResponseEntity<Void> deletePlace(@PathVariable Long placeId);
}
