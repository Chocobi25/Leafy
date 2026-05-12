package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.place.application.PlaceService;
import com.chocobi.leafy.trip.dto.request.TripPlaceRequest;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.dto.request.TripPlacesListRequest;
import com.chocobi.leafy.trip.infra.entity.Trip;
import com.chocobi.leafy.trip.infra.entity.TripPlace;
import com.chocobi.leafy.trip.infra.repository.TripPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class TripPlaceService {
    private final TripPlaceRepository tripPlaceRepository;
    private final PlaceService placeService;

    public void saveInitialTripPlaces(Trip trip, TripPlacesListRequest request) {
        List<TripPlace> tripPlaces = request.getPlaces().stream()
                .map(placeReq -> TripPlace.builder()
                        .trip(trip)
                        .place(placeService.getPlace(placeReq.getPlaceId()))
                        .memo(placeReq.getMemo())
                        .build())
                .toList();

        tripPlaceRepository.saveAll(tripPlaces);
    }

    @Transactional
    public void editTripPlaceDetails(Trip trip, List<TripPlaceRequest> request) {
        // 기존 TripPlace 삭제
        deleteTripPlaces(trip);

        // 새로운 TripPlace 저장
        List<TripPlace> tripPlaces = request.stream()
                .map(placeReq -> TripPlace.builder()
                        .trip(trip)
                        .place(placeService.getPlace(placeReq.getPlaceId()))
                        .memo(placeReq.getMemo())
                        .dayIndex(placeReq.getDayIndex())
                        .visitOrder(placeReq.getVisitOrder())
                        .build())
                .toList();

        tripPlaceRepository.saveAll(tripPlaces);
    }

    @Transactional(readOnly = true)
    public List<TripPlaceResponse> getTripPlaces(Long tripId) {
        List<TripPlace> places = tripPlaceRepository.findByTripId(tripId);

        return places.stream()
                .filter(tripPlace -> tripPlace.getPlace() != null) // null 체크 추가
                .map(TripPlaceResponse::toDTO)
                .toList();
    }

    public void deleteTripPlace(Long tripPlaceId) {
        tripPlaceRepository.deleteById(tripPlaceId);
    }

    @Transactional
    public void deleteTripPlaces(Trip trip) {
        tripPlaceRepository.deleteAllByTrip(trip);
    }

    public TripPlace getTripPlaceById(Long tripPlaceId) {
        return tripPlaceRepository.findById(tripPlaceId).orElse(null);
    }

}