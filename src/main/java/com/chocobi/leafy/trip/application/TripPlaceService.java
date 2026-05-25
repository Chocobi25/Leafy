package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.place.application.PlaceService;
import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import com.chocobi.leafy.trip.dto.request.CreateTripPlaceRequest;
import com.chocobi.leafy.trip.dto.request.UpdateTripPlaceRequest;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.dto.response.TripPlacesResponse;
import com.chocobi.leafy.trip.infra.TripFindService;
import com.chocobi.leafy.trip.infra.TripPlaceCommandService;
import com.chocobi.leafy.trip.infra.TripPlaceFindService;
import com.chocobi.leafy.trip.infra.entity.TripEntity;
import com.chocobi.leafy.trip.infra.entity.TripPlaceEntity;
import com.chocobi.leafy.trip.vo.TripPlaceError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TripPlaceService {
    private final TripPlaceFindService tripPlaceFindService;
    private final TripPlaceCommandService tripPlaceCommandService;
    private final PlaceService placeService;
    private final TripFindService tripFindService;

    @Transactional
    public TripPlacesResponse createTripPlaces(Long tripId, List<CreateTripPlaceRequest> request, Long userId) {
        TripEntity trip = tripFindService.findOwnedTrip(tripId, userId);
        validateDuplicateVisitOrders(request.stream()
                .map(placeReq -> new VisitOrderKey(placeReq.dayIndex(), placeReq.visitOrder()))
                .toList());

        if (tripPlaceFindService.hasTripPlaces(tripId)) {
            throw new CustomException(TripPlaceError.TRIP_PLACES_ALREADY_EXIST);
        }

        Map<Long, PlaceEntity> placeMap = getPlaceMap(request.stream()
                .map(CreateTripPlaceRequest::placeId)
                .toList());

        List<TripPlaceEntity> tripPlaces = request.stream()
                .map(placeReq -> TripPlaceEntity.builder()
                        .trip(trip)
                        .place(placeMap.get(placeReq.placeId()))
                        .memo(placeReq.memo())
                        .dayIndex(placeReq.dayIndex())
                        .visitOrder(placeReq.visitOrder())
                        .build())
                .toList();

        List<TripPlaceResponse> savedTripPlaces = tripPlaceCommandService.saveAll(tripPlaces).stream()
                .map(TripPlaceResponse::from)
                .toList();
        trip.markRouteStale();

        return TripPlacesResponse.from(trip, savedTripPlaces);
    }

    @Transactional
    public TripPlacesResponse updateTripPlaces(Long tripId, List<UpdateTripPlaceRequest> request, Long userId) {
        TripEntity trip = tripFindService.findOwnedTrip(tripId, userId);
        validateDuplicateTripPlaceIds(request);
        validateDuplicateVisitOrders(request.stream()
                .map(placeReq -> new VisitOrderKey(placeReq.dayIndex(), placeReq.visitOrder()))
                .toList());

        List<TripPlaceEntity> existingTripPlaces = tripPlaceFindService.findOrderedTripPlaces(tripId);
        if (existingTripPlaces.isEmpty()) {
            throw new CustomException(TripPlaceError.TRIP_PLACES_NOT_CREATED);
        }

        Map<Long, TripPlaceEntity> existingTripPlaceMap = existingTripPlaces.stream()
                .collect(Collectors.toMap(TripPlaceEntity::getId, Function.identity()));
        Set<Long> requestedTripPlaceIds = request.stream()
                .map(UpdateTripPlaceRequest::tripPlaceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!existingTripPlaceMap.keySet().containsAll(requestedTripPlaceIds)) {
            throw new CustomException(TripPlaceError.TRIP_PLACE_NOT_FOUND);
        }

        List<TripPlaceEntity> deletedTripPlaces = existingTripPlaces.stream()
                .filter(tripPlace -> !requestedTripPlaceIds.contains(tripPlace.getId()))
                .toList();
        boolean hasRouteAffectingUpdate = hasAnyRouteAffectingUpdate(request, existingTripPlaceMap);
        boolean hasDeletedTripPlaces = !deletedTripPlaces.isEmpty();
        boolean hasRouteAffectingChange = hasRouteAffectingUpdate || hasDeletedTripPlaces;
        boolean needsPlaceLookup = needsPlaceLookup(request, existingTripPlaceMap);
        Map<Long, PlaceEntity> placeMap = needsPlaceLookup
                ? getPlaceMap(request.stream()
                        .map(UpdateTripPlaceRequest::placeId)
                        .toList())
                : Map.of();

        List<TripPlaceEntity> newTripPlaces = new ArrayList<>();
        for (UpdateTripPlaceRequest placeReq : request) {
            if (placeReq.tripPlaceId() == null) {
                PlaceEntity place = placeMap.get(placeReq.placeId());
                newTripPlaces.add(TripPlaceEntity.builder()
                        .trip(trip)
                        .place(place)
                        .memo(placeReq.memo())
                        .dayIndex(placeReq.dayIndex())
                        .visitOrder(placeReq.visitOrder())
                        .build());
                continue;
            }

            TripPlaceEntity existingTripPlace = existingTripPlaceMap.get(placeReq.tripPlaceId());
            if (isRouteAffectingUpdate(existingTripPlace, placeReq)) {
                if (isPlaceChanged(existingTripPlace, placeReq)) {
                    PlaceEntity place = placeMap.get(placeReq.placeId());
                    existingTripPlace.updateDetails(place, placeReq.dayIndex(), placeReq.visitOrder(), placeReq.memo());
                    continue;
                }

                existingTripPlace.updateSchedule(placeReq.dayIndex(), placeReq.visitOrder(), placeReq.memo());
                continue;
            }

            existingTripPlace.updateMemo(placeReq.memo());
        }

        if (hasDeletedTripPlaces) {
            tripPlaceCommandService.deleteAll(deletedTripPlaces);
        }

        if (!newTripPlaces.isEmpty()) {
            tripPlaceCommandService.saveAll(newTripPlaces);
        }

        if (hasRouteAffectingChange) {
            trip.markRouteStale();
        }

        return TripPlacesResponse.from(trip, getTripPlaces(tripId));
    }

    @Transactional(readOnly = true)
    public List<TripPlaceResponse> getTripPlaces(Long tripId) {
        List<TripPlaceEntity> places = tripPlaceFindService.findOrderedTripPlaces(tripId);

        return places.stream()
                .map(this::toTripPlaceResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TripPlaceEntity getTripPlaceById(Long tripPlaceId) {
        return tripPlaceFindService.findTripPlace(tripPlaceId);
    }

    private void validateDuplicateTripPlaceIds(List<UpdateTripPlaceRequest> request) {
        List<Long> tripPlaceIds = request.stream()
                .map(UpdateTripPlaceRequest::tripPlaceId)
                .filter(Objects::nonNull)
                .toList();
        Set<Long> uniqueTripPlaceIds = Set.copyOf(tripPlaceIds);

        if (tripPlaceIds.size() != uniqueTripPlaceIds.size()) {
            throw new CustomException(TripPlaceError.DUPLICATE_TRIP_PLACE_REQUEST);
        }
    }

    private void validateDuplicateVisitOrders(List<VisitOrderKey> visitOrders) {
        Set<VisitOrderKey> uniqueVisitOrders = Set.copyOf(visitOrders);

        if (visitOrders.size() != uniqueVisitOrders.size()) {
            throw new CustomException(TripPlaceError.DUPLICATE_TRIP_PLACE_REQUEST);
        }
    }

    private Map<Long, PlaceEntity> getPlaceMap(List<Long> placeIds) {
        List<Long> uniquePlaceIds = placeIds.stream()
                .distinct()
                .toList();

        return placeService.getPlaces(uniquePlaceIds).stream()
                .collect(Collectors.toMap(PlaceEntity::getId, Function.identity(), (first, second) -> first));
    }

    private boolean hasAnyRouteAffectingUpdate(
            List<UpdateTripPlaceRequest> request,
            Map<Long, TripPlaceEntity> existingTripPlaceMap
    ) {
        return request.stream().anyMatch(placeReq -> placeReq.tripPlaceId() == null
                || isRouteAffectingUpdate(existingTripPlaceMap.get(placeReq.tripPlaceId()), placeReq));
    }

    private boolean needsPlaceLookup(
            List<UpdateTripPlaceRequest> request,
            Map<Long, TripPlaceEntity> existingTripPlaceMap
    ) {
        return request.stream().anyMatch(placeReq -> placeReq.tripPlaceId() == null
                || isPlaceChanged(existingTripPlaceMap.get(placeReq.tripPlaceId()), placeReq));
    }

    private boolean isRouteAffectingUpdate(TripPlaceEntity existingTripPlace, UpdateTripPlaceRequest request) {
        return isPlaceChanged(existingTripPlace, request)
                || !Objects.equals(existingTripPlace.getDayIndex(), request.dayIndex())
                || !Objects.equals(existingTripPlace.getVisitOrder(), request.visitOrder());
    }

    private boolean isPlaceChanged(TripPlaceEntity existingTripPlace, UpdateTripPlaceRequest request) {
        return !Objects.equals(getPlaceId(existingTripPlace), request.placeId());
    }

    private TripPlaceResponse toTripPlaceResponse(TripPlaceEntity tripPlace) {
        validatePlaceExists(tripPlace);
        return TripPlaceResponse.from(tripPlace);
    }

    private Long getPlaceId(TripPlaceEntity tripPlace) {
        validatePlaceExists(tripPlace);
        return tripPlace.getPlace().getId();
    }

    private void validatePlaceExists(TripPlaceEntity tripPlace) {
        if (tripPlace.getPlace() == null) {
            throw new CustomException(TripPlaceError.TRIP_PLACE_NOT_FOUND);
        }
    }

    private record VisitOrderKey(Integer dayIndex, Integer visitOrder) {
    }

}
