package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.place.entity.Image;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.fetcher.image.ImageSearchClient;
import com.chocobi.leafy.place.fetcher.image.dto.SearchResponse;
import com.chocobi.leafy.place.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceImageProcessor implements ItemProcessor<Place, List<Image>> {
    private final ImageSearchClient imageSearchClient;
    private final ImageRepository imageRepository;

    @Override
    public List<Image> process(Place place) throws Exception {
        // DB에서 이미지 존재 여부 확인
        if (imageRepository.existsByPlace(place)) {
            log.info("이미 사진이 존재하는 장소입니다. 스킵: {}", place.getTitle());
            return null;
        }

        // 검색 쿼리 조합 (카테고리 + 지역 + 저작권 + 제목)
        String query = String.format("%s %s %s %s",
                place.getCategory().name(),
                place.getAddress().split(" ")[0],
                place.getCopyright(),
                place.getTitle()
        );

        // 네이버 이미지 검색 API 호출
        SearchResponse response = imageSearchClient.searchImage(query);

        // 검색 결과를 Image 엔티티로 변환
        if (response != null && response.getItems() != null && !response.getItems().isEmpty()) {
            // 최대 5개의 사진만 가져오기
            List<Image> images = response.getItems().stream()
                    .limit(5)
                    .map(imageItem -> {
                        Image image = new Image();
                        image.setUrl(imageItem.getLink());
                        image.setCopyright(imageItem.getTitle());
                        image.setPlace(place);
                        return image;
                    })
                    .toList();

            log.info("검색된 이미지 {}개를 저장합니다. 장소: {}", images.size(), place.getTitle());
            return images;
        }

        log.warn("장소에 대한 이미지 검색 결과가 없습니다: {}", place.getTitle());
        return null;
    }
}