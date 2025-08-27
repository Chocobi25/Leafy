package com.chocobi.leafy.trip.service;

import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.service.PlaceService;
import com.chocobi.leafy.trip.dto.*;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripPlace;
import com.chocobi.leafy.trip.repository.TripPlaceRepository;
import com.chocobi.leafy.trip.repository.TripRepository;
import com.chocobi.leafy.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final UserService userService;
    private final PlaceService placeService;

    public Long createTrip(TripRequest tripRequest) {
        return tripRepository.save(Trip.builder()
                .user(userService.findByKakaoId(tripRequest.getUser_id()))
                .title(tripRequest.getTitle())
                .start_date(tripRequest.getStart_date())
                .end_date(tripRequest.getEnd_date())
                .build()).getId();
    }

    public void deleteTrip(Long tripId) {
        tripRepository.deleteById(tripId);
    }

    public void updateTrip(Long tripId, TripRequest tripRequest) {
        Trip trip = getTripById(tripId);

        trip.update(tripRequest.getTitle(), tripRequest.getStart_date(), tripRequest.getEnd_date());
        tripRepository.save(trip);
    }

    public Trip getTripById(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 여행입니다."));
    }
}
