package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.place.infra.entity.PlaceStaging;
import com.chocobi.leafy.place.fetcher.eco.EcoService;
import com.chocobi.leafy.place.fetcher.farm.FarmService;
import com.chocobi.leafy.place.fetcher.rural.RuralService;
import com.chocobi.leafy.place.fetcher.theme.ThemeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceStagingTasklet implements Tasklet {

    private final EcoService ecoService;
    private final RuralService ruralService;
    private final ThemeService themeService;
    private final FarmService farmService;

    private final PlaceStagingRepository placeStagingRepository;

    private List<PlaceStaging> fetchAllPlaces() {
        List<PlaceStaging> allPlaces = new ArrayList<>();

        try {
            List<PlaceStaging> ecoPlaces = ecoService.getPlaceStaging();
            ecoPlaces.stream().filter(p -> p.getAddress() != null).forEach(allPlaces::add);
            log.info("EcoService fetched {} places", ecoPlaces.size());
        } catch (Exception e) {
            log.error("EcoService fetch failed", e);
        }

        try {
            List<PlaceStaging> ruralPlaces = ruralService.getPlaceStaging();
            ruralPlaces.stream().filter(p -> p.getAddress() != null).forEach(allPlaces::add);
            log.info("RuralService fetched {} places", ruralPlaces.size());
        } catch (Exception e) {
            log.error("RuralService fetch failed", e);
        }

        try {
            List<PlaceStaging> themePlaces = themeService.getPlaceStaging();
            themePlaces.stream().filter(p -> p.getAddress() != null).forEach(allPlaces::add);
            log.info("ThemeService fetched {} places", themePlaces.size());
        } catch (Exception e) {
            log.error("ThemeService fetch failed", e);
        }

        try {
            List<PlaceStaging> farmPlaces = farmService.getPlaceStaging();
            farmPlaces.stream().filter(p -> p.getAddress() != null).forEach(allPlaces::add);
            log.info("FarmService fetched {} places", farmPlaces.size());
        } catch (Exception e) {
            log.error("FarmService fetch failed", e);
        }

        return allPlaces;
    }

    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Starting PlaceStagingTasklet...");

        // 1. 기존 데이터 삭제 (항상 최신만 유지)
        placeStagingRepository.deleteAll();
        log.info("PlaceStaging table cleared.");

        // 2. API 호출
        List<PlaceStaging> allPlaces = fetchAllPlaces();

        // 3. 결과 저장
        if (!allPlaces.isEmpty()) {
            placeStagingRepository.saveAll(allPlaces);
            log.info("Saved {} places into PlaceStaging.", allPlaces.size());
        } else {
            log.warn("No places fetched. Skipping save.");
        }

        log.info("PlaceStagingTasklet finished.");
        return RepeatStatus.FINISHED;
    }
}
