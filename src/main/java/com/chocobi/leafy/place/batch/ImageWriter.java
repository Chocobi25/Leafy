package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.place.entity.Image;
import com.chocobi.leafy.place.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageWriter implements ItemWriter<List<Image>> {
    private final ImageRepository imageRepository;

    @Override
    public void write(Chunk<? extends List<Image>> chunk) throws Exception {
        log.info("Saving a chunk of image lists. Chunk size: {}.", chunk.size());

        List<Image> allImages = chunk.getItems().stream()
                .flatMap(List::stream)
                .toList();

        if (!allImages.isEmpty()) {
            imageRepository.saveAll(allImages);
            log.info("Successfully saved {} images.", allImages.size());
        } else {
            log.warn("No images to save in this chunk.");
        }
    }
}
