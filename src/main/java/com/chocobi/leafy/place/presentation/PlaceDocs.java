package com.chocobi.leafy.place.presentation;

import com.chocobi.leafy.global.exception.ErrorResponse;
import com.chocobi.leafy.place.dto.response.ExternalPlaceDetailResponse;
import com.chocobi.leafy.place.dto.response.ExternalPlaceListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "장소 API", description = "추천 장소 조회")
public interface PlaceDocs {

    @Operation(summary = "추천 장소 목록 조회")
    @ApiResponse(responseCode = "200",
            content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = ExternalPlaceListResponse.class)),
                    examples = @ExampleObject(value = """
                            [
                                {
                                    "id": 1,
                                    "title": "남산서울타워",
                                    "category": "자연",
                                    "address": "서울 용산구 남산공원길 105",
                                    "region": "서울"
                                }
                            ]
                            """)
            ))
    ResponseEntity<List<ExternalPlaceListResponse>> getPlaces();

    @Operation(summary = "추천 장소 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = ExternalPlaceDetailResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "id": 1,
                                        "title": "남산서울타워",
                                        "description": "서울의 대표적인 전망 명소입니다.",
                                        "category": "자연",
                                        "address": "서울 용산구 남산공원길 105",
                                        "region": "서울",
                                        "latitude": 37.55117,
                                        "longitude": 126.9882,
                                        "tel": "02-1234-5678",
                                        "url": "https://example.com/place/1",
                                        "copyright": "한국관광공사"
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
    ResponseEntity<ExternalPlaceDetailResponse> getPlace(@PathVariable Long placeId);
}
