package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.place.application.PlaceService;
import com.chocobi.leafy.trip.dto.request.TripPlaceRequest;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.dto.request.TripPlacesListRequest;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
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

    public void saveInitialTripPlaces(TripEntity trip, TripPlacesListRequest request) {
        List<TripPlaceEntity> tripPlaces = request.getPlaces().stream()
                .map(placeReq -> TripPlaceEntity.builder()
                        .trip(trip)
                        .place(placeService.getPlace(placeReq.getPlaceId()))
                        .memo(placeReq.getMemo())
                        .build())
                .toList();

        tripPlaceRepository.saveAll(tripPlaces);
    }

    @Transactional
    public void editTripPlaceDetails(TripEntity trip, List<TripPlaceRequest> request) {
        // 기존 TripPlace 삭제
        deleteTripPlaces(trip);

        // 새로운 TripPlace 저장
        List<TripPlaceEntity> tripPlaces = request.stream()
                .map(placeReq -> TripPlaceEntity.builder()
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
        List<TripPlaceEntity> places = tripPlaceRepository.findByTrip_Id(tripId);

        return places.stream()
                .filter(tripPlace -> tripPlace.getPlace() != null) // null 체크 추가
                .map(TripPlaceResponse::toDTO)
                .toList();
    }

    public void deleteTripPlace(Long tripPlaceId) {
        tripPlaceRepository.deleteById(tripPlaceId);
    }

    @Transactional
    public void deleteTripPlaces(TripEntity trip) {
        tripPlaceRepository.deleteAllByTrip(trip);
    }

    public TripPlaceEntity getTripPlaceById(Long tripPlaceId) {
        return tripPlaceRepository.findById(tripPlaceId).orElse(null);
    }

}
