package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.place.infra.entity.Image;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.fetcher.image.ImageSearchService;
import com.chocobi.leafy.place.infra.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceImageProcessor implements ItemProcessor<ExternalPlaceEntity, List<Image>> {
    private final ImageSearchService imageSearchService;
    private final ImageRepository imageRepository;

    @Override
    public List<Image> process(ExternalPlaceEntity place) throws Exception {
        // DB에 이미지가 존재하면 스킵
        if (imageRepository.existsByPlace(place)) {
            log.info("이미 사진이 존재하는 장소입니다. 스킵: {}", place.getTitle());
            return null;
        }

        // imageSearchService의 Mono를 .block()으로 기다린 후 결과를 가져옵니다.
        List<Image> images = imageSearchService.findImagesForPlace(place).block();

        if (images != null && !images.isEmpty()) {
            log.info("검색된 이미지 {}개를 저장합니다. 장소: {}", images.size(), place.getTitle());
            return images;
        }

        log.warn("장소에 대한 이미지 검색 결과가 없습니다: {}", place.getTitle());
        return null;
    }
}