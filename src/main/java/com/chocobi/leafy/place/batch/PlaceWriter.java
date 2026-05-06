package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceWriter implements ItemWriter<ExternalPlaceEntity> {
    private final PlaceRepository placeRepository;

    @Override
    public void write(Chunk<? extends ExternalPlaceEntity> chunk) throws Exception {
        for (ExternalPlaceEntity item : chunk.getItems()) {
            if (placeRepository.findByTitle(item.getTitle()).isEmpty()) {
                // 존재하지 않으면 새로운 장소로 판단하고 저장
                log.info("Inserting new place: {}", item.getTitle());
                placeRepository.save(item);
            } else {
                // 이미 존재하면 아무 작업 없이 스킵
                log.info("Place already exists. Skipping: {}", item.getTitle());
            }
        }
    }
}
