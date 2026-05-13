package com.chocobi.leafy.trip.presentation;

import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.trip.application.TripService;
import com.chocobi.leafy.trip.client.TransCoordDTO;
import com.chocobi.leafy.trip.vo.TripError;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
@Validated
public class TripCertificationController {

    private final TripService tripService;

    @PostMapping("/{tripId}/certify")
    public ResponseEntity<String> certifyTrip(@PathVariable @Positive Long tripId,
                                              @Valid @RequestBody TransCoordDTO transCoordDTO) {
        if (!tripId.equals(transCoordDTO.getTripId())) {
            throw new CustomException(TripError.INVALID_TRIP_REQUEST);
        }

        tripService.certifyTrip(transCoordDTO);
        return ResponseEntity.ok("여행이 성공적으로 인증되었습니다.");
    }
}
