package com.chocobi.leafy.config;

import com.chocobi.leafy.place.batch.*;
import com.chocobi.leafy.place.infra.entity.Image;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceStaging;
import com.chocobi.leafy.place.fetcher.image.ImageSearchService;
import com.chocobi.leafy.place.infra.repository.ImageRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier; // Qualifier import 추가
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;


@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    // Tasklet과 Processor, Writer 빈들은 @Bean으로 관리되므로, @Qualifier를 사용해 명확히 주입받습니다.
    private final PlaceStagingTasklet placeStagingTasklet;
    private final PlaceGeocodeProcessor placeGeocodeProcessor;
    private final PlaceWriter placeWriter;
    private final ImageSearchService imageSearchService;
    private final ImageRepository imageRepository;
    private final ImageWriter imageWriter;

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("batch-image-thread-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "placeStagingStep")
    public Step placeStagingStep() {
        return new StepBuilder("placeStagingStep", jobRepository)
                .tasklet(placeStagingTasklet, transactionManager)
                .build();
    }

    @Bean(name = "placeStagingReader")
    public ItemReader<PlaceStaging> placeStagingReader() {
        return new JpaPagingItemReaderBuilder<PlaceStaging>()
                .name("placeStagingReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(100)
                .queryString("SELECT p FROM PlaceStaging p")
                .build();
    }

    @Bean(name = "placeReader")
    public ItemReader<ExternalPlaceEntity> placeReader() {
        return new JpaPagingItemReaderBuilder<ExternalPlaceEntity>()
                .name("placeReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(100)
                .queryString("SELECT p FROM Place p")
                .build();
    }

    @Bean(name = "placeProcessStep")
    public Step placeProcessStep(@Qualifier("placeStagingReader") ItemReader<PlaceStaging> placeStagingReader,
                                 @Qualifier("placeGeocodeProcessor") PlaceGeocodeProcessor placeGeocodeProcessor,
                                 @Qualifier("placeWriter") PlaceWriter placeWriter) {
        return new StepBuilder("placeProcessStep", jobRepository)
                .<PlaceStaging, ExternalPlaceEntity>chunk(100, transactionManager)
                .reader(placeStagingReader)
                .processor(placeGeocodeProcessor)
                .writer(placeWriter)
                .build();
    }

    @Bean
    @StepScope
    public PlaceImageProcessor placeImageProcessor() {
        return new PlaceImageProcessor(imageSearchService, imageRepository);
    }

    @Bean(name = "imageStep")
    public Step imageStep(@Qualifier("placeReader") ItemReader<ExternalPlaceEntity> placeReader,
                          @Qualifier("placeImageProcessor") PlaceImageProcessor placeImageProcessor,
                          @Qualifier("imageWriter") ImageWriter imageWriter,
                          @Qualifier("taskExecutor") TaskExecutor taskExecutor) {
        return new StepBuilder("imageStep", jobRepository)
                .<ExternalPlaceEntity, List<Image>>chunk(100, transactionManager)
                .reader(placeReader)
                .processor(placeImageProcessor)
                .writer(imageWriter)
                .taskExecutor(taskExecutor)
                .build();
    }


    @Bean
    public Job placeDataJob(@Qualifier("placeStagingStep") Step placeStagingStep,
                            @Qualifier("placeProcessStep") Step placeProcessStep,
                            @Qualifier("imageStep") Step imageStep) {
        return new JobBuilder("placeDataJob", jobRepository)
                .start(placeStagingStep)
                .next(placeProcessStep)
                .next(imageStep)
                .build();
    }
}