package com.chocobi.leafy.trip.service;

import com.chocobi.leafy.fcm.service.FCMService;
import com.chocobi.leafy.trip.dto.*;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripStatus;
import com.chocobi.leafy.trip.repository.TripRepository;
import com.chocobi.leafy.user.entity.User;
import com.chocobi.leafy.user.service.UserService;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
@AllArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final UserService userService;
    private final FCMService fcmService;

    public Long createTrip(TripRequest tripRequest, Long kakaoId) {
        Trip trip = Trip.builder()
                .user(userService.findByKakaoId(kakaoId))
                .title(tripRequest.getTitle())
                .startDate(tripRequest.getStart_date())
                .endDate(tripRequest.getEnd_date())
                .build();
        tripRepository.save(trip);
        return trip.getId();
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

    public List<TripDTO> getTripsByUser(Long userId){
        User user = userService.findByKakaoId(userId);
        List<Trip> trips = tripRepository.findAllByUser(user);

        return trips.stream()
                .map(TripDTO::fromEntity)
                .toList();
    }

    public void changeTripStatus(Long tripId, TripStatus tripStatus) {
        Trip trip = getTripById(tripId);
        trip.editStatus(tripStatus);
        tripRepository.save(trip);
    }

    public void saveTrip(Trip trip){
        tripRepository.save(trip);
    }
}
