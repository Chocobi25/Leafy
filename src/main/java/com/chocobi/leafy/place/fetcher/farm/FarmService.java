package com.chocobi.leafy.place.fetcher.farm;

import com.chocobi.leafy.place.common.util.PlaceConstants;
import com.chocobi.leafy.place.fetcher.farm.dto.FarmDetailApiResponse;
import com.chocobi.leafy.place.fetcher.farm.dto.FarmDetailItem;
import com.chocobi.leafy.place.fetcher.farm.dto.FarmListApiResponse;
import com.chocobi.leafy.place.entity.Category;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.entity.PlaceSourceType;
import com.chocobi.leafy.place.repository.PlaceRepository;
import com.chocobi.leafy.place.fetcher.kakao.GeocodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FarmService {

    private final WebClient farmWebClient;
    private final PlaceRepository placeRepository;
    private final GeocodeService geocodeService;

    @Value("${farm.api.key}")
    private String apiKey;

    // 목록 조회
    public FarmListApiResponse searchFarmList() {
        return farmWebClient.get()
                .uri(this::buildFarmListUri)
                .retrieve()
                .bodyToMono(FarmListApiResponse.class)
                .block();
    }

    // 상세 조회
    public FarmDetailApiResponse searchFarmDetail(String contentId) {
        return farmWebClient.get()
                .uri(uriBuilder -> buildFarmDetailUri(uriBuilder, contentId))
                .retrieve()
                .bodyToMono(FarmDetailApiResponse.class)
                .block();
    }


    private URI buildFarmListUri(UriBuilder builder) {
        return builder.path(PlaceConstants.FARM_LIST)
                .queryParam("apiKey", apiKey)
                .queryParam("numOfRows", PlaceConstants.DEFAULT_NUM_OF_ROWS)
                .queryParam("pageNo", PlaceConstants.PAGE_NO)
                .build();
    }

    private URI buildFarmDetailUri(UriBuilder builder, String contentId) {
        return builder.path(PlaceConstants.FARM_DETAIL)
                .queryParam("apiKey", apiKey)
                .queryParam("cntntsNo", contentId)
                .build();
    }

    public void saveFarmPlace() {
        FarmListApiResponse farmListApiResponse = searchFarmList();
        List<Place> list = farmListApiResponse.getBody().getFarmListItems().getItem().stream()
                .map(farmListItem -> {
                    FarmDetailApiResponse farmDetailApiResponse = searchFarmDetail(farmListItem.getCntntsNo());
                    FarmDetailItem item = farmDetailApiResponse.getBody().getFarmDetailItem();

                    double[] coords = geocodeService.getCoordinatesFromAddress(item.getLocplc());

                    return Place.builder()
                            .title(item.getCntntsSj())
                            .description(item.getSmm())
                            .category(Category.FOOD)
                            .address(item.getLocplc())
                            .longitude(coords[0])
                            .latitude(coords[1])
                            .tel(item.getTelno())
                            .url(item.getUrl())
                            .imageUrl(farmListItem.getThumbImgUrl())
                            .sourceType(PlaceSourceType.API)
                            .build();
                })
                .toList();

        placeRepository.saveAll(list);
    }
}
