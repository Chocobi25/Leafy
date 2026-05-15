package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.global.entity.RegionEntity;
import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.global.service.RegionFindService;
import com.chocobi.leafy.place.fetcher.kakao.dto.Address;
import com.chocobi.leafy.trip.client.TransCoordDTO;
import com.chocobi.leafy.trip.client.TransCoordResponse;
import com.chocobi.leafy.trip.client.TranscodeClient;
import com.chocobi.leafy.trip.dto.*;
import com.chocobi.leafy.trip.dto.request.CreateTripRequest;
import com.chocobi.leafy.trip.dto.request.TripUpdateRequest;
import com.chocobi.leafy.trip.dto.response.TripDetailResponse;
import com.chocobi.leafy.trip.dto.response.TripListResponse;
import com.chocobi.leafy.trip.dto.response.TripSaveResponse;
import com.chocobi.leafy.trip.infra.TripCommandService;
import com.chocobi.leafy.trip.infra.TripFindService;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripStatus;
import com.chocobi.leafy.trip.vo.TripError;
import com.chocobi.leafy.user.infra.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TripService {
    private final TripFindService tripFindService;
    private final TripCommandService tripCommandService;
    private final UserService userService;
    private final TripPlaceService tripPlaceService;
    private final TripSegmentService tripSegmentService;
    private final TranscodeClient transcodeClient;
    private final RegionFindService regionFindService;

    @Transactional
    public TripSaveResponse createTrip(CreateTripRequest createTripRequest, Long userId) {

        RegionEntity departure = regionFindService.findRegion(createTripRequest.departure());
        RegionEntity arrival = regionFindService.findRegion(createTripRequest.arrival());

        TripEntity trip = TripEntity.builder()
                .user(userService.findById(userId))
                .title(createTripRequest.title())
                .startDate(createTripRequest.startDate())
                .endDate(createTripRequest.endDate())
                .departure(departure)
                .arrival(arrival)
                .build();

        return TripSaveResponse.from(tripCommandService.save(trip));
    }

    @Transactional(readOnly = true)
    public List<TripListResponse> getTrips(Long userId) {
        return tripFindService.findTripsByUserId(userId).stream()
                .map(TripListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TripDetailResponse getTripDetails(Long tripId, Long userId) {
        TripEntity trip = tripFindService.findOwnedTripDetail(tripId, userId);

        List<TripSegmentDTO> tripSegments = tripSegmentService.getTripSegments(tripId);
        return TripDetailResponse.from(trip, tripSegments);
    }

    @Transactional
    public void deleteTrip(Long tripId, Long userId) {

        TripEntity trip = findOwnedTrip(tripId, userId);

        tripSegmentService.deleteTripSegments(trip);
        tripPlaceService.deleteTripPlaces(trip);
        tripCommandService.delete(trip);
    }

    @Transactional
    public TripDetailResponse updateTripInfo(Long tripId, TripUpdateRequest request, Long userId) {

        TripEntity trip = tripFindService.findOwnedTripDetail(tripId, userId);

        trip.update(request.title(), request.startDate(), request.endDate());
        List<TripSegmentDTO> tripSegments = tripSegmentService.getTripSegments(tripId);

        return TripDetailResponse.from(trip, tripSegments);
    }

    @Transactional(readOnly = true)
    public TripEntity findOwnedTrip(Long tripId, Long userId) {
        return tripFindService.findOwnedTrip(tripId, userId);
    }

    @Transactional
    public void certifyTrip(Long tripId, TransCoordDTO transCoordDTO, Long userId) {
        TripEntity trip = findOwnedTrip(tripId, userId);

        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new CustomException(TripError.TRIP_NOT_IN_PROGRESS);
        }

        if (trip.getCertificationAt() != null) {
            throw new CustomException(TripError.TRIP_ALREADY_CERTIFIED);
        }

        TransCoordResponse addressResponse = transcodeClient.requestGeocode(transCoordDTO);
        String currentRegionName = extractRegionName(addressResponse);
        RegionEntity currentRegion = regionFindService.findRegion(currentRegionName);

        if (!trip.getArrival().getId().equals(currentRegion.getId())) {
            throw new CustomException(TripError.TRIP_LOCATION_MISMATCH);
        }

        try {
            trip.certify();
        } catch (IllegalStateException e) {
            throw new CustomException(TripError.TRIP_ALREADY_CERTIFIED);
        }

        tripCommandService.save(trip);
    }

    @Transactional
    public void changeTripStatusForScheduler(Long tripId, TripStatus tripStatus) {
        TripEntity trip = tripFindService.findTrip(tripId);
        trip.editStatus(tripStatus);
    }

    @Transactional
    public void changeOwnedTripStatus(Long tripId, TripStatus tripStatus, Long userId) {
        TripEntity trip = findOwnedTrip(tripId, userId);
        trip.editStatus(tripStatus);
    }

    private String extractRegionName(TransCoordResponse addressResponse) {
        if (addressResponse == null
                || addressResponse.getDocuments() == null
                || addressResponse.getDocuments().isEmpty()
                || addressResponse.getDocuments().getFirst() == null) {
            throw new CustomException(TripError.TRIP_LOCATION_UNAVAILABLE);
        }

        Address address = addressResponse.getDocuments().getFirst().getAddress();
        if (address == null || address.getRegion_1depth_name() == null || address.getRegion_1depth_name().isBlank()) {
            throw new CustomException(TripError.TRIP_LOCATION_UNAVAILABLE);
        }

        return address.getRegion_1depth_name();
    }
}
