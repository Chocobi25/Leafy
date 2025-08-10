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

import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final TripPlaceRepository tripPlaceRepository;
    private final UserService userService;
    private final PlaceService placeService;

    public Long createTrip(TripRequest tripRequest) {
        Trip trip = Trip.builder()
                .user(userService.findByKakaoId(tripRequest.getUser_id()))
                .title(tripRequest.getTitle())
                .carbon_saved(0)
                .start_date(tripRequest.getStart_date())
                .end_date(tripRequest.getEnd_date())
                .build();
        tripRepository.save(trip);
        return trip.getId();
    }

    public void saveTripPlace(TripPlaceListRequest placeListRequest) {
        List<TripPlaceRequest> sortedList = placeListRequest.getPlaceList().stream()
                .sorted(Comparator.comparing(TripPlaceRequest::getVisitDate)
                        .thenComparing(TripPlaceRequest::getVisitOrder))
                .toList();

        Trip trip = tripRepository.findById(placeListRequest.getTripId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Trip ID"));

        for(TripPlaceRequest placeRequest : sortedList) {
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

}
