package com.chocobi.leafy.place.batch;

import com.chocobi.leafy.place.application.ExternalPlaceSynchronizer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalPlaceSyncTasklet implements Tasklet {
    private final List<ExternalPlaceSynchronizer> externalPlaceSynchronizers;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Starting external place sync");
        int collectedCount = externalPlaceSynchronizers.stream()
                .mapToInt(ExternalPlaceSynchronizer::sync)
                .sum();
        contribution.incrementWriteCount(collectedCount);
        log.info("Finished external place sync. collected={}", collectedCount);
        return RepeatStatus.FINISHED;
    }
}
