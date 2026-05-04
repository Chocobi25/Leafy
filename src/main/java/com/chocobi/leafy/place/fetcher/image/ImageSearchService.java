package com.chocobi.leafy.place.fetcher.image;

import com.chocobi.leafy.place.infra.entity.Image;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.fetcher.image.dto.ImageItem;
import com.chocobi.leafy.place.fetcher.image.dto.SearchResponse;
import com.chocobi.leafy.place.fetcher.image.dto.TourImageItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageSearchService {
    private final TourImageClient tourImageClient;
    private final NaverImageClient naverImageClient;

    public Mono<List<Image>> findImagesForPlace(ExternalPlaceEntity place) {
        return tourImageClient.fetchTourImages(place.getTitle())
                .flatMap(apiResponse -> {
                    if (apiResponse != null && apiResponse.getResponse() != null) {
                        List<TourImageItem> items = apiResponse.getResponse().getBody().getItems().getItem();

                        if (items != null && !items.isEmpty()) {
                            log.info("✔️ 한국관광공사 API에서 이미지 {}개를 찾았습니다. 장소: {}", items.size(), place.getTitle());
                            return Mono.just(mapTourApiItemsToImages(items.stream().limit(5).toList(), place));
                        }
                    }
                    log.warn("❌ 한국관광공사 API 호출 실패. 네이버로 대체합니다.");
                    return searchNaverImages(place);
                })
                .onErrorResume(e -> {
                    log.warn("❌ 한국관광공사 API 호출 중 오류 발생. 네이버로 대체합니다: {}", e.getMessage());
                    return searchNaverImages(place);
                });
    }

    private Mono<List<Image>> searchNaverImages(ExternalPlaceEntity place) {
        log.info("➡️ 네이버 API로 이미지 검색을 시작합니다. 장소: {}", place.getTitle());
        List<String> queries = List.of(
                //String.format("%s %s", place.getRegionGroup(), place.getTitle()),
                //String.format("%s %s 사진", place.getRegionGroup(), place.getTitle()),
                String.format("%s 내부", place.getTitle()),
                String.format("%s 외관", place.getTitle())
        );

        return Flux.fromIterable(queries)
                // ✅ 각 API 호출 사이에 500ms 지연을 추가합니다.
                .delayElements(Duration.ofMillis(500))
                .flatMap(query -> naverImageClient.searchImage(query)
                        // ✅ 각 개별 호출이 실패하더라도 다른 호출에 영향을 주지 않도록 처리합니다.
                        .onErrorResume(e -> {
                            log.warn("❌ 네이버 API 호출 중 오류 발생. 쿼리: {}, 오류: {}", query, e.getMessage());
                            return Mono.empty(); // 실패한 쿼리는 무시하고 다음 쿼리를 계속 진행합니다.
                        })
                )
                .collectList()
                .map(responses -> {
                    List<Image> naverImages = new ArrayList<>();
                    for (SearchResponse naverResponse : responses) {
                        if (naverResponse != null && naverResponse.getItems() != null) {
                            List<Image> foundImages = mapNaverApiItemsToImages(naverResponse.getItems(), place);
                            int remaining = 5 - naverImages.size();
                            if (remaining > 0) {
                                naverImages.addAll(foundImages.stream().limit(remaining).toList());
                            }
                        }
                        if (naverImages.size() >= 5) {
                            break;
                        }
                    }
                    if (!naverImages.isEmpty()) {
                        log.info("✔️ 네이버 API로 이미지 {}개를 찾았습니다. 장소: {}", naverImages.size(), place.getTitle());
                    } else {
                        log.warn("❌ 모든 API에서 장소에 대한 이미지 검색 결과가 없습니다: {}", place.getTitle());
                    }
                    return naverImages;
                });
    }

    private List<Image> mapTourApiItemsToImages(List<TourImageItem> tourItems, ExternalPlaceEntity place) {
        return tourItems.stream()
                .map(item -> {
                    Image image = new Image();
                    image.setUrl(item.getGalWebImageUrl());
                    image.setCopyright(item.getGalPhotographer());
                    image.setPlace(place);
                    return image;
                })
                .collect(Collectors.toList());
    }

    private List<Image> mapNaverApiItemsToImages(List<ImageItem> naverItems, ExternalPlaceEntity place) {
        return naverItems.stream()
                .map(item -> {
                    Image image = new Image();
                    image.setUrl(item.getLink());
                    image.setCopyright(item.getTitle());
                    image.setPlace(place);
                    return image;
                })
                .collect(Collectors.toList());
    }
}