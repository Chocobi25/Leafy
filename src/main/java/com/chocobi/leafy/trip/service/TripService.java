package com.chocobi.leafy.trip.service;

import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.service.PlaceService;
import com.chocobi.leafy.trip.dto.TripPlaceListRequest;
import com.chocobi.leafy.trip.dto.TripPlaceRequest;
import com.chocobi.leafy.trip.dto.TripPlaceResponse;
import com.chocobi.leafy.trip.dto.TripRequest;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripPlace;
import com.chocobi.leafy.trip.repository.TripPlaceRepository;
import com.chocobi.leafy.trip.repository.TripRepository;
import com.chocobi.leafy.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final TripPlaceRepository tripPlaceRepository;
    private final UserService userService;
    private final PlaceService placeService;

    public Long createTrip(TripRequest tripRequest, Long kakaoId) {
        Trip trip = Trip.builder()
                .user(userService.findByKakaoId(kakaoId))
                .title(tripRequest.getTitle())
                .carbon_saved(0)
                .start_date(tripRequest.getStart_date())
                .end_date(tripRequest.getEnd_date())
                .build();
        tripRepository.save(trip);
        return trip.getId();
    }

    @Transactional
    public void saveTripPlace(TripPlaceListRequest placeListRequest) {
        System.out.println("TripPlace 저장 시작 - TripId: " + placeListRequest.getTripId());
        System.out.println("PlaceList 크기: " + placeListRequest.getPlaceList().size());
        
        tripPlaceRepository.deleteAllByTripId(placeListRequest.getTripId());

        List<TripPlaceRequest> sortedList = placeListRequest.getPlaceList().stream()
                .sorted(Comparator.comparing(TripPlaceRequest::getVisitDate)
                        .thenComparing(TripPlaceRequest::getVisitOrder))
                .toList();

        Trip trip = getTripById(placeListRequest.getTripId());

        for(TripPlaceRequest placeRequest : sortedList) {
            System.out.println("Place 조회 시도 - PlaceId: " + placeRequest.getPlaceId());
            Place place = placeService.getPlaceById(placeRequest.getPlaceId());

            TripPlace tripPlace = TripPlace.builder()
                    .trip(trip)
                    .place(place)
                    .visitDate(placeRequest.getVisitDate())
                    .visit_order(placeRequest.getVisitOrder())
                    .build();

            tripPlaceRepository.save(tripPlace);
        }
    }

    public List<TripPlaceResponse> getTripPlaces(Long tripId) {
        List<TripPlace> places = tripPlaceRepository.findByTripId(tripId);

        return places.stream()
                .map(TripPlaceResponse::toDTO)
                .toList();
    }

    public void deleteTrip(Long tripId) {
        tripRepository.deleteById(tripId);
    }

    public void updateTrip(Long tripId, TripRequest tripRequest) {
        Trip trip = getTripById(tripId);

        trip.update(tripRequest.getTitle(), tripRequest.getStart_date(), tripRequest.getEnd_date());
        tripRepository.save(trip);
    }

    private Trip getTripById(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 여행입니다."));
    }
}
