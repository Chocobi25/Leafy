package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.place.infra.entity.PlaceImageEntity;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.repository.PlaceImageRepository;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceImageProcessor implements ItemProcessor<ExternalPlaceEntity, List<PlaceImageEntity>> {
    private final PlaceImageRepository placeImageRepository;

    @Override
    public List<PlaceImageEntity> process(ExternalPlaceEntity place) throws Exception {
        // DB에 이미지가 존재하면 스킵
        if (placeImageRepository.existsByPlace(place)) {
            log.info("이미 사진이 존재하는 장소입니다. 스킵: {}", place.getTitle());
            return null;
        }

        // imageSearchService의 Mono를 .block()으로 기다린 후 결과를 가져옵니다.
        List<PlaceImageEntity> placeImageEntities = new ArrayList<>(); // TODO: 나중에 변경 예정

        if (placeImageEntities != null && !placeImageEntities.isEmpty()) {
            log.info("검색된 이미지 {}개를 저장합니다. 장소: {}", placeImageEntities.size(), place.getTitle());
            return placeImageEntities;
        }

        log.warn("장소에 대한 이미지 검색 결과가 없습니다: {}", place.getTitle());
        return null;
    }
}