package com.chocobi.leafy.trip.service;

import com.chocobi.leafy.place.service.PlaceService;
import com.chocobi.leafy.trip.dto.TripPlaceRequest;
import com.chocobi.leafy.trip.dto.TripPlaceResponse;
import com.chocobi.leafy.trip.dto.TripPlacesListRequest;
import com.chocobi.leafy.trip.entity.Trip;
import com.chocobi.leafy.trip.entity.TripPlace;
import com.chocobi.leafy.trip.repository.TripPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TripPlaceService {
    private final TripPlaceRepository tripPlaceRepository;
    private final TripService tripService;
    private final PlaceService placeService;

    public void saveTripPlaces(TripPlacesListRequest request) {
        Trip trip = tripService.getTripById(request.getTripId());

        List<TripPlace> tripPlaces = request.getPlaces().stream()
                .map(placeReq -> TripPlace.builder()
                        .trip(trip)
                        .place(placeService.getPlaceById(placeReq.getPlaceId()))
                        .memo(placeReq.getMemo())
                        .build())
                .toList();

        tripPlaceRepository.saveAll(tripPlaces);
    }

    public void updateTripPlaceDetails(TripPlacesListRequest request) {
        Trip trip = tripService.getTripById(request.getTripId());

        List<TripPlace> tripPlaces = tripPlaceRepository.findByTripId(request.getTripId());

        Map<Long, TripPlace> tripPlaceMap = tripPlaces.stream()
                .collect(Collectors.toMap(tp -> tp.getPlace().getId(), tp -> tp));

        for (TripPlaceRequest req : request.getPlaces()) {
            TripPlace tp = tripPlaceMap.get(req.getPlaceId());
            if (tp != null) {
                tp.updateDetails(req.getDayIndex(), req.getVisitOrder(), req.getMemo());
            }
        }

        tripPlaceRepository.saveAll(tripPlaces);
    }


    @Transactional(readOnly = true)
    public List<TripPlaceResponse> getTripPlaces(Long tripId) {
        List<TripPlace> places = tripPlaceRepository.findByTripId(tripId);

        return places.stream()
                .map(TripPlaceResponse::toDTO)
                .toList();
    }

    public void deleteTripPlace(Long tripPlaceId) {
        tripPlaceRepository.deleteById(tripPlaceId);
    }
}
