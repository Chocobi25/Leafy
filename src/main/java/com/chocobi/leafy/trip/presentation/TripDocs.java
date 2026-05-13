package com.chocobi.leafy.trip.presentation;

import com.chocobi.leafy.global.exception.ErrorResponse;
import com.chocobi.leafy.global.response.SuccessResponse;
import com.chocobi.leafy.trip.dto.request.TripRequest;
import com.chocobi.leafy.trip.dto.request.TripUpdateRequest;
import com.chocobi.leafy.trip.dto.response.TripDetailResponse;
import com.chocobi.leafy.trip.dto.response.TripListResponse;
import com.chocobi.leafy.trip.dto.response.TripSaveResponse;
import com.chocobi.leafy.trip.dto.response.TripUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "여행 API", description = "여행 기본 CRUD")
public interface TripDocs {

    @Operation(summary = "여행 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = TripSaveResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "code": "SUCCESS",
                                        "message": "요청이 성공했습니다.",
                                        "data": {
                                            "tripId": 1,
                                            "status": "CREATING",
                                            "createdAt": "2026-05-14T10:00:00"
                                        }
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "400",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "status": 400,
                                        "code": "INVALID_TRIP_DATE",
                                        "message": "여행 날짜가 올바르지 않습니다.",
                                        "timestamp": "2026-05-14T10:00:00"
                                    }
                                    """)
                    ))
    })
    ResponseEntity<SuccessResponse<TripSaveResponse>> createTrip(
            @Valid @RequestBody TripRequest tripRequest,
            Authentication authentication
    );

    @Operation(summary = "내 여행 목록 조회")
    @ApiResponse(responseCode = "200",
            content = @Content(
                    schema = @Schema(implementation = TripListResponse.class),
                    examples = @ExampleObject(value = """
                            {
                                "code": "SUCCESS",
                                "message": "요청이 성공했습니다.",
                                "data": [
                                    {
                                        "tripId": 1,
                                        "title": "서울 힐링 여행",
                                        "startDate": "2026-06-01",
                                        "endDate": "2026-06-03",
                                        "status": "CREATING",
                                        "createdAt": "2026-05-14T10:00:00"
                                    }
                                ]
                            }
                            """)
            ))
    ResponseEntity<SuccessResponse<List<TripListResponse>>> getTrips(Authentication authentication);

    @Operation(summary = "여행 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = TripDetailResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "code": "SUCCESS",
                                        "message": "요청이 성공했습니다.",
                                        "data": {
                                            "tripId": 1,
                                            "title": "서울 힐링 여행",
                                            "startDate": "2026-06-01",
                                            "endDate": "2026-06-03",
                                            "departure": "서울",
                                            "arrival": "부산",
                                            "carbonSaved": 5.2,
                                            "carbonEmission": 12.5,
                                            "status": "CREATING",
                                            "certificationAt": null,
                                            "userId": 1,
                                            "createdAt": "2026-05-14T10:00:00",
                                            "updatedAt": "2026-05-14T10:00:00",
                                            "tripSegments": []
                                        }
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "403",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "status": 403,
                                        "code": "TRIP_ACCESS_DENIED",
                                        "message": "해당 여행에 접근할 권한이 없습니다.",
                                        "timestamp": "2026-05-14T10:00:00"
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "404",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "status": 404,
                                        "code": "TRIP_NOT_FOUND",
                                        "message": "존재하지 않는 여행입니다.",
                                        "timestamp": "2026-05-14T10:00:00"
                                    }
                                    """)
                    ))
    })
    ResponseEntity<SuccessResponse<TripDetailResponse>> getTripDetails(
            @PathVariable @Positive Long tripId,
            Authentication authentication
    );

    @Operation(summary = "여행 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "403",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "status": 403,
                                        "code": "TRIP_ACCESS_DENIED",
                                        "message": "해당 여행에 접근할 권한이 없습니다.",
                                        "timestamp": "2026-05-14T10:00:00"
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "404",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "status": 404,
                                        "code": "TRIP_NOT_FOUND",
                                        "message": "존재하지 않는 여행입니다.",
                                        "timestamp": "2026-05-14T10:00:00"
                                    }
                                    """)
                    ))
    })
    ResponseEntity<Void> deleteTrip(
            @PathVariable @Positive Long tripId,
            Authentication authentication
    );

    @Operation(summary = "여행 기본 정보 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = TripUpdateResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "code": "SUCCESS",
                                        "message": "요청이 성공했습니다.",
                                        "data": {
                                            "tripId": 1,
                                            "title": "수정된 여행",
                                            "startDate": "2026-06-02",
                                            "endDate": "2026-06-04",
                                            "status": "CREATING"
                                        }
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "400",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "status": 400,
                                        "code": "INVALID_INPUT_VALUE",
                                        "message": "입력값이 올바르지 않습니다.",
                                        "timestamp": "2026-05-14T10:00:00"
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "403",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "status": 403,
                                        "code": "TRIP_ACCESS_DENIED",
                                        "message": "해당 여행에 접근할 권한이 없습니다.",
                                        "timestamp": "2026-05-14T10:00:00"
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "404",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "status": 404,
                                        "code": "TRIP_NOT_FOUND",
                                        "message": "존재하지 않는 여행입니다.",
                                        "timestamp": "2026-05-14T10:00:00"
                                    }
                                    """)
                    ))
    })
    ResponseEntity<SuccessResponse<TripUpdateResponse>> updateTrip(
            @PathVariable @Positive Long tripId,
            @Valid @RequestBody TripUpdateRequest request,
            Authentication authentication
    );
}
