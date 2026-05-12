package com.chocobi.leafy.trip.application;

import com.chocobi.leafy.place.infra.entity.RegionGroup;
import com.chocobi.leafy.trip.client.TransCoordDTO;
import com.chocobi.leafy.trip.client.TransCoordResponse;
import com.chocobi.leafy.trip.client.TranscodeClient;
import com.chocobi.leafy.trip.dto.*;
import com.chocobi.leafy.trip.dto.request.TripRequest;
import com.chocobi.leafy.trip.dto.response.TripPlaceResponse;
import com.chocobi.leafy.trip.infra.entity.Trip;
import com.chocobi.leafy.trip.infra.entity.TripStatus;
import com.chocobi.leafy.trip.infra.repository.TripRepository;
import com.chocobi.leafy.user.infra.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
@AllArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final UserService userService;
    private final TripPlaceService tripPlaceService;
    private final TripSegmentService tripSegmentService;
    private final TranscodeClient transcodeClient;

    @Transactional
    public Long createTrip(TripRequest tripRequest, Long kakaoId) {
        Trip trip = Trip.builder()
                .user(userService.findById(kakaoId))  // TODO: 로직 동작 확인
                .title(tripRequest.getTitle())
                .startDate(tripRequest.getStart_date())
                .endDate(tripRequest.getEnd_date())
                .departure(RegionGroup.fromRegionName(tripRequest.getDeparture()))
                .arrival(RegionGroup.fromRegionName(tripRequest.getArrival()))
                .build();
        tripRepository.save(trip);
        return trip.getId();
    }

    @Transactional
    public void deleteTrip(Long tripId) {
        Trip trip = getTripById(tripId);
        tripPlaceService.deleteTripPlaces(trip);
        tripSegmentService.deleteTripSegments(trip);
        tripRepository.deleteById(tripId);
    }

    public Trip getTripById(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 여행입니다."));
    }

    public void changeTripStatus(Long tripId, TripStatus tripStatus) {
        Trip trip = getTripById(tripId);
        trip.editStatus(tripStatus);
        tripRepository.save(trip);
    }

    public void saveTrip(Trip trip){
        tripRepository.save(trip);
    }

    @Transactional
    public TripDetailsDTO getTripDetails(Long tripId) {
        Trip trip = getTripById(tripId);

        List<TripPlaceResponse> tripPlaces = tripPlaceService.getTripPlaces(tripId);
        List<TripSegmentDTO> tripSegments = tripSegmentService.getTripSegments(tripId);
        TripDTO tripDTO = TripDTO.fromEntity(trip);

        return new TripDetailsDTO(tripDTO, tripSegments);
    }

    @Transactional
    public void certifyTrip(TransCoordDTO transCoordDTO) {
        Trip trip = getTripById(transCoordDTO.getTripId());

        // 1. 여행 상태가 IN_PROGRESS인지 확인
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new IllegalStateException("진행 중인 여행만 인증할 수 있습니다.");
        }

        // 2. 카카오 API를 호출하여 현재 좌표의 주소 정보 가져오기
        TransCoordResponse addressResponse = transcodeClient.requestGeocode(transCoordDTO);

        if (addressResponse == null || addressResponse.getDocuments().isEmpty()) {
            throw new IllegalStateException("현재 위치의 주소 정보를 가져올 수 없습니다. 다시 시도해주세요.");
        }

        String currentRegionStr = addressResponse.getDocuments().get(0).getAddress().getRegion_1depth_name();
        RegionGroup currentRegion = RegionGroup.fromRegionName(currentRegionStr);

        // 3. 여행의 도착 도시와 현재 도시가 같은지 비교
        if (!trip.getArrival().equals(currentRegion)) {
            // 도시가 다르면 예외 발생
            throw new IllegalArgumentException("현재 위치가 여행 도착 지역(" + trip.getArrival() + ")과 다릅니다. 위치를 다시 확인해주세요.");
        }

        // 4. 모든 조건 충족 시, 인증 시간 기록 및 상태 변경
        trip.certify();

        // 변경사항 저장
        tripRepository.save(trip);
    }


    public void updateTripInfo(Trip trip, String title, LocalDate startDate, LocalDate endDate) {
        trip.update(title, startDate, endDate); // 엔티티에 이미 있는 update 메서드 활용
        tripRepository.save(trip);
    }
}