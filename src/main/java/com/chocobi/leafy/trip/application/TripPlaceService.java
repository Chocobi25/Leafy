package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.place.application.PlaceService;
import com.chocobi.leafy.trip.infra.TripPlaceCommandService;
import com.chocobi.leafy.trip.infra.TripPlaceFindService;
import com.chocobi.leafy.trip.dto.request.TripPlaceRequest;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TripPlaceService {
    private final TripPlaceFindService tripPlaceFindService;
    private final TripPlaceCommandService tripPlaceCommandService;
    private final PlaceService placeService;
    private final TripService tripService;

    public List<TripPlaceResponse> saveTripPlaces(Long tripId, List<TripPlaceRequest> request, Long userId) {
        TripEntity trip = tripService.getOwnedTrip(tripId, userId);

        tripPlaceCommandService.deleteAllByTrip(trip);

        List<TripPlaceEntity> tripPlaces = request.stream()
                .map(placeReq -> TripPlaceEntity.builder()
                        .trip(trip)
                        .place(placeService.getPlace(placeReq.getPlaceId()))
                        .memo(placeReq.getMemo())
                        .dayIndex(placeReq.getDayIndex())
                        .visitOrder(placeReq.getVisitOrder())
                        .build())
                .toList();

        return tripPlaceCommandService.saveAll(tripPlaces).stream()
                .map(TripPlaceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TripPlaceResponse> getTripPlaces(Long tripId) {
        List<TripPlaceEntity> places = tripPlaceFindService.findOrderedTripPlaces(tripId);

        return places.stream()
                .filter(tripPlace -> tripPlace.getPlace() != null)
                .map(TripPlaceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TripPlaceEntity getTripPlaceById(Long tripPlaceId) {
        return tripPlaceFindService.findTripPlace(tripPlaceId);
    }

}
