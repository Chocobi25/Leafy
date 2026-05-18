package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.place.infra.entity.PlaceImageEntity;
import com.chocobi.leafy.place.infra.repository.PlaceImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageWriter implements ItemWriter<List<PlaceImageEntity>> {
    private final PlaceImageRepository placeImageRepository;

    @Override
    public void write(Chunk<? extends List<PlaceImageEntity>> chunk) throws Exception {
        log.info("Saving a chunk of image lists. Chunk size: {}.", chunk.size());

        List<PlaceImageEntity> allPlaceImageEntities = chunk.getItems().stream()
                .flatMap(List::stream)
                .toList();

        if (!allPlaceImageEntities.isEmpty()) {
            placeImageRepository.saveAll(allPlaceImageEntities);
            log.info("Successfully saved {} images.", allPlaceImageEntities.size());
        } else {
            log.warn("No images to save in this chunk.");
        }
    }
}
