package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceWriter implements ItemWriter<Place> {
    private final PlaceRepository placeRepository;

    @Override
    public void write(Chunk<? extends Place> chunk) throws Exception {
        log.info("Saving a chunk of {} places.", chunk.size());

        placeRepository.saveAll(chunk.getItems());

        log.info("Successfully saved {} places.", chunk.size());
    }
}
