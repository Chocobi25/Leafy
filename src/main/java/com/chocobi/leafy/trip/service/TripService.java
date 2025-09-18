package com.chocobi.leafy.trip.service;

import com.chocobi.leafy.place.entity.RegionGroup;
import com.chocobi.leafy.trip.dto.*;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripStatus;
import com.chocobi.leafy.trip.repository.TripRepository;
import com.chocobi.leafy.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final UserService userService;
    private final TripPlaceService tripPlaceService;
    private final TripSegmentService tripSegmentService;

    @Transactional
    public Long createTrip(TripRequest tripRequest, Long kakaoId) {
        Trip trip = Trip.builder()
                .user(userService.findByKakaoId(kakaoId))
                .title(tripRequest.getTitle())
                .startDate(tripRequest.getStart_date())
                .endDate(tripRequest.getEnd_date())
                .departure(RegionGroup.fromRegionName(tripRequest.getDeparture()))
                .arrival(RegionGroup.fromRegionName(tripRequest.getArrival()))
                .build();
        tripRepository.save(trip);
        return trip.getId();
    }

    @Transactional
    public void deleteTrip(Long tripId) {
        Trip trip = getTripById(tripId);
        tripPlaceService.deleteTripPlaces(trip);
        tripSegmentService.deleteTripSegments(trip);
        tripRepository.deleteById(tripId);
    }

    public Trip getTripById(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 여행입니다."));
    }

    public void changeTripStatus(Long tripId, TripStatus tripStatus) {
        Trip trip = getTripById(tripId);
        trip.editStatus(tripStatus);
        tripRepository.save(trip);
    }

    public void saveTrip(Trip trip){
        tripRepository.save(trip);
    }

    @Transactional
    public TripDetailsDTO getTripDetails(Long tripId) {
        Trip trip = getTripById(tripId);

        List<TripPlaceResponse> tripPlaces = tripPlaceService.getTripPlaces(tripId);
        List<TripSegmentDTO> tripSegments = tripSegmentService.getTripSegments(tripId);
        TripDTO tripDTO = TripDTO.fromEntity(trip);

        return new TripDetailsDTO(tripDTO, tripSegments);
    }
}