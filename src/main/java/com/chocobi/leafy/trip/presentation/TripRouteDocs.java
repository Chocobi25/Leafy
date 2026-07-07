package com.chocobi.leafy.trip.presentation;

import com.chocobi.leafy.global.exception.ErrorResponse;
import com.chocobi.leafy.global.response.SuccessResponse;
import com.chocobi.leafy.trip.dto.request.CompleteTripRequest;
import com.chocobi.leafy.trip.dto.request.TripRouteSummaryRequest;
import com.chocobi.leafy.trip.dto.response.CompleteTripResponse;
import com.chocobi.leafy.trip.dto.response.TripCarRouteResponse;
import com.chocobi.leafy.trip.dto.response.TripPublicRouteResponse;
import com.chocobi.leafy.trip.dto.response.TripRouteCandidateSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "여행 경로 API", description = "여행 경로 계산, 요약 조회, 완료 처리")
public interface TripRouteDocs {

    @Operation(
            summary = "여행 경로 후보 확정 및 여행 완료",
            description = "transport 요청 본문 값은 car 또는 public을 사용합니다. 여행 장소가 변경된 뒤 경로를 다시 계산하지 않았다면 확정할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = CompleteTripResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "code": "SUCCESS",
                                        "message": "요청이 성공했습니다.",
                                        "data": {
                                            "tripId": 1
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
                                        "code": "STALE_TRIP_ROUTE_CANDIDATE",
                                        "message": "여행 장소가 변경되어 경로를 다시 계산해야 합니다.",
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
                                        "code": "TRIP_ROUTE_OPTION_NOT_FOUND",
                                        "message": "존재하지 않는 여행 경로 후보입니다.",
                                        "timestamp": "2026-05-14T10:00:00"
                                    }
                                    """)
                    ))
    })
    ResponseEntity<SuccessResponse<CompleteTripResponse>> completeTrip(
            @PathVariable @Positive Long tripId,
            @Valid @RequestBody CompleteTripRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "여행 경로 후보 요약 조회",
            description = "transport 쿼리 파라미터는 CAR 또는 PUBLIC을 사용합니다."
    )
    @ApiResponse(responseCode = "200",
            content = @Content(
                    schema = @Schema(implementation = TripRouteCandidateSummaryResponse.class),
                    examples = @ExampleObject(value = """
                            {
                                "code": "SUCCESS",
                                "message": "요청이 성공했습니다.",
                                "data": {
                                    "totalDuration": 180,
                                    "totalCarbonEmission": 12.5
                                }
                            }
                            """)
            ))
    ResponseEntity<SuccessResponse<TripRouteCandidateSummaryResponse>> getRouteSummary(
            @PathVariable @Positive Long tripId,
            @Valid @ModelAttribute TripRouteSummaryRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "자동차 경로 계산 및 후보 저장",
            description = "여행 장소가 2개 이상이어야 경로 계산이 가능합니다."
    )
    @ApiResponse(responseCode = "200",
            content = @Content(
                    schema = @Schema(implementation = TripCarRouteResponse.class),
                    examples = @ExampleObject(value = """
                            {
                                "code": "SUCCESS",
                                "message": "요청이 성공했습니다.",
                                "data": {
                                    "distance": 12000.0,
                                    "duration": 2400,
                                    "carbonEmission": 2.3
                                }
                            }
                            """)
            ))
    ResponseEntity<SuccessResponse<TripCarRouteResponse>> calculateCarRoute(
            @PathVariable @Positive Long tripId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "대중교통 경로 계산 및 후보 저장",
            description = "여행 장소가 2개 이상이어야 경로 계산이 가능합니다."
    )
    @ApiResponse(responseCode = "200",
            content = @Content(
                    schema = @Schema(implementation = TripPublicRouteResponse.class),
                    examples = @ExampleObject(value = """
                            {
                                "code": "SUCCESS",
                                "message": "요청이 성공했습니다.",
                                "data": [
                                    {
                                        "pathType": 1,
                                        "totalTime": 3600,
                                        "totalDistance": 15000.0,
                                        "carbonEmission": 1.2,
                                        "maxCarbonEmission": 4.8,
                                        "busDistance": 5000,
                                        "subwayDistance": 8000,
                                        "trainDistance": 2000,
                                        "airplaneDistance": 0
                                    }
                                ]
                            }
                            """)
            ))
    ResponseEntity<SuccessResponse<List<TripPublicRouteResponse>>> calculatePublicRoute(
            @PathVariable @Positive Long tripId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId
    );
}
