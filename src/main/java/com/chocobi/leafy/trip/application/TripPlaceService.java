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

        Map<Long, PlaceEntity> placeMap = getPlaceMapForCreateRequests(request);

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

        TripPlaceUpdateContext updateContext = TripPlaceUpdateContext.from(
                tripPlaceFindService.findOrderedTripPlaces(tripId),
                request
        );
        updateContext.validateRequestedTripPlacesExist();

        TripPlaceUpdatePlan updatePlan = createUpdatePlan(request, updateContext);

        Map<Long, PlaceEntity> placeMap = updatePlan.placeLookupRequired()
                ? getPlaceMapForUpdateRequests(request)
                : Map.of();

        List<TripPlaceEntity> newTripPlaces = applyTripPlaceUpdates(trip, request, updateContext, placeMap);

        if (updatePlan.hasDeletedTripPlaces()) {
            tripPlaceCommandService.deleteAll(updatePlan.deletedTripPlaces());
        }

        if (!newTripPlaces.isEmpty()) {
            tripPlaceCommandService.saveAll(newTripPlaces);
        }

        if (updatePlan.routeRecalculationRequired()) {
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

    private Map<Long, PlaceEntity> getPlaceMapForCreateRequests(List<CreateTripPlaceRequest> request) {
        return getPlaceMap(request.stream()
                .map(CreateTripPlaceRequest::placeId)
                .toList());
    }

    private Map<Long, PlaceEntity> getPlaceMapForUpdateRequests(List<UpdateTripPlaceRequest> request) {
        return getPlaceMap(request.stream()
                .map(UpdateTripPlaceRequest::placeId)
                .toList());
    }

    private List<TripPlaceEntity> applyTripPlaceUpdates(
            TripEntity trip,
            List<UpdateTripPlaceRequest> request,
            TripPlaceUpdateContext updateContext,
            Map<Long, PlaceEntity> placeMap
    ) {
        List<TripPlaceEntity> newTripPlaces = new ArrayList<>();
        for (UpdateTripPlaceRequest placeReq : request) {
            if (placeReq.tripPlaceId() == null) {
                newTripPlaces.add(createNewTripPlace(trip, placeReq, placeMap));
                continue;
            }

            TripPlaceEntity existingTripPlace = updateContext.getExistingTripPlace(placeReq.tripPlaceId());
            updateExistingTripPlace(existingTripPlace, placeReq, placeMap);
        }

        return newTripPlaces;
    }

    private TripPlaceEntity createNewTripPlace(
            TripEntity trip,
            UpdateTripPlaceRequest request,
            Map<Long, PlaceEntity> placeMap
    ) {
        return TripPlaceEntity.builder()
                .trip(trip)
                .place(placeMap.get(request.placeId()))
                .memo(request.memo())
                .dayIndex(request.dayIndex())
                .visitOrder(request.visitOrder())
                .build();
    }

    private void updateExistingTripPlace(
            TripPlaceEntity existingTripPlace,
            UpdateTripPlaceRequest request,
            Map<Long, PlaceEntity> placeMap
    ) {
        if (isPlaceChanged(existingTripPlace, request)) {
            PlaceEntity place = placeMap.get(request.placeId());
            existingTripPlace.updateDetails(place, request.dayIndex(), request.visitOrder(), request.memo());
            return;
        }

        if (isScheduleChanged(existingTripPlace, request)) {
            existingTripPlace.updateSchedule(request.dayIndex(), request.visitOrder(), request.memo());
            return;
        }

        existingTripPlace.updateMemo(request.memo());
    }

    private boolean isPlaceChanged(TripPlaceEntity existingTripPlace, UpdateTripPlaceRequest request) {
        return !Objects.equals(getPlaceId(existingTripPlace), request.placeId());
    }

    private boolean isScheduleChanged(TripPlaceEntity existingTripPlace, UpdateTripPlaceRequest request) {
        return !Objects.equals(existingTripPlace.getDayIndex(), request.dayIndex())
                || !Objects.equals(existingTripPlace.getVisitOrder(), request.visitOrder());
    }

    private boolean isRouteRecalculationRequired(TripPlaceEntity existingTripPlace, UpdateTripPlaceRequest request) {
        return isPlaceChanged(existingTripPlace, request)
                || !Objects.equals(existingTripPlace.getDayIndex(), request.dayIndex())
                || !Objects.equals(existingTripPlace.getVisitOrder(), request.visitOrder());
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

    private TripPlaceUpdatePlan createUpdatePlan(
            List<UpdateTripPlaceRequest> request,
            TripPlaceUpdateContext updateContext
    ) {
        boolean routeRecalculationRequiredByUpdate = request.stream()
                .anyMatch(placeReq -> placeReq.tripPlaceId() == null
                        || isRouteRecalculationRequired(
                                updateContext.getExistingTripPlace(placeReq.tripPlaceId()),
                                placeReq
                        ));

        boolean placeLookupRequired = request.stream()
                .anyMatch(placeReq -> placeReq.tripPlaceId() == null
                        || isPlaceChanged(updateContext.getExistingTripPlace(placeReq.tripPlaceId()), placeReq));

        return new TripPlaceUpdatePlan(
                updateContext.findDeletedTripPlaces(),
                routeRecalculationRequiredByUpdate,
                placeLookupRequired
        );
    }

    private record VisitOrderKey(Integer dayIndex, Integer visitOrder) {
    }

    private record TripPlaceUpdateContext(
            List<TripPlaceEntity> existingTripPlaces,
            Map<Long, TripPlaceEntity> existingTripPlaceMap,
            Set<Long> requestedTripPlaceIds
    ) {
        private static TripPlaceUpdateContext from(
                List<TripPlaceEntity> existingTripPlaces,
                List<UpdateTripPlaceRequest> request
        ) {

            if (existingTripPlaces.isEmpty()) {
                throw new CustomException(TripPlaceError.TRIP_PLACES_NOT_CREATED);
            }

            Map<Long, TripPlaceEntity> existingTripPlaceMap = existingTripPlaces.stream()
                    .collect(Collectors.toMap(TripPlaceEntity::getId, Function.identity()));

            Set<Long> requestedTripPlaceIds = request.stream()
                    .map(UpdateTripPlaceRequest::tripPlaceId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            return new TripPlaceUpdateContext(
                    existingTripPlaces, existingTripPlaceMap, requestedTripPlaceIds
            );
        }

        private void validateRequestedTripPlacesExist() {
            if (!existingTripPlaceMap.keySet().containsAll(requestedTripPlaceIds)) {
                throw new CustomException(TripPlaceError.TRIP_PLACE_NOT_FOUND);
            }
        }

        private TripPlaceEntity getExistingTripPlace(Long tripPlaceId) {
            return existingTripPlaceMap.get(tripPlaceId);
        }

        private List<TripPlaceEntity> findDeletedTripPlaces() {
            return existingTripPlaces.stream()
                    .filter(tripPlace -> !requestedTripPlaceIds.contains(tripPlace.getId()))
                    .toList();
        }
    }

    private record TripPlaceUpdatePlan(
            List<TripPlaceEntity> deletedTripPlaces,
            boolean routeRecalculationRequiredByUpdate,
            boolean placeLookupRequired
    ) {
        private boolean hasDeletedTripPlaces() {
            return !deletedTripPlaces.isEmpty();
        }

        private boolean routeRecalculationRequired() {
            return routeRecalculationRequiredByUpdate || hasDeletedTripPlaces();
        }
    }
}
