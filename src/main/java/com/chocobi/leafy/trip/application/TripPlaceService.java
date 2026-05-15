package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.place.application.PlaceService;
import com.chocobi.leafy.trip.infra.TripPlaceCommandService;
import com.chocobi.leafy.trip.infra.TripPlaceFindService;
import com.chocobi.leafy.trip.infra.TripFindService;
import com.chocobi.leafy.trip.dto.request.TripPlaceRequest;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.dto.request.TripPlacesListRequest;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class TripPlaceService {
    private final TripPlaceFindService tripPlaceFindService;
    private final TripPlaceCommandService tripPlaceCommandService;
    private final PlaceService placeService;
    private final TripFindService tripFindService;

    public void saveInitialTripPlaces(TripPlacesListRequest request, Long userId) {
        TripEntity trip = tripFindService.findOwnedTrip(request.getTripId(), userId);
        List<TripPlaceEntity> tripPlaces = buildTripPlaces(trip, request.getPlaces());

        tripPlaceCommandService.saveAll(tripPlaces);
    }

    @Transactional
    public void editTripPlaceDetails(TripPlacesListRequest request, Long userId) {
        TripEntity trip = tripFindService.findOwnedTrip(request.getTripId(), userId);
        editTripPlaceDetails(trip, request.getPlaces());
    }

    @Transactional
    public void editTripPlaceDetails(TripEntity trip, List<TripPlaceRequest> places) {
        // 기존 TripPlace 삭제
        deleteTripPlaces(trip);

        // 새로운 TripPlace 저장
        List<TripPlaceEntity> tripPlaces = buildTripPlaces(trip, places);

        tripPlaceCommandService.saveAll(tripPlaces);
    }

    private List<TripPlaceEntity> buildTripPlaces(TripEntity trip, List<TripPlaceRequest> places) {
        return places.stream()
                .map(placeReq -> TripPlaceEntity.builder()
                        .trip(trip)
                        .place(placeService.getPlace(placeReq.getPlaceId()))
                        .memo(placeReq.getMemo())
                        .dayIndex(placeReq.getDayIndex())
                        .visitOrder(placeReq.getVisitOrder())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TripPlaceResponse> getTripPlaces(Long tripId) {
        List<TripPlaceEntity> places = tripPlaceFindService.findTripPlacesByTripId(tripId);

        return places.stream()
                .filter(tripPlace -> tripPlace.getPlace() != null) // null 체크 추가
                .map(TripPlaceResponse::toDTO)
                .toList();
    }

    public void deleteTripPlace(Long tripPlaceId) {
        TripPlaceEntity tripPlace = tripPlaceFindService.findTripPlace(tripPlaceId);
        tripPlaceCommandService.delete(tripPlace);
    }

    @Transactional
    public void deleteTripPlaces(TripEntity trip) {
        tripPlaceCommandService.deleteAllByTrip(trip);
    }

    @Transactional(readOnly = true)
    public TripPlaceEntity getTripPlaceById(Long tripPlaceId) {
        return tripPlaceFindService.findTripPlace(tripPlaceId);
    }

}
