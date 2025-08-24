package com.chocobi.leafy.config;

import com.chocobi.leafy.place.batch.PlaceGeocodeProcessor;
import com.chocobi.leafy.place.batch.PlaceStagingTasklet;
import com.chocobi.leafy.place.batch.PlaceWriter;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.entity.PlaceStaging;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private final PlaceStagingTasklet placeStagingTasklet;
    private final PlaceGeocodeProcessor placeGeocodeProcessor;
    private final PlaceWriter placeWriter;

    @Bean
    public Step placeStagingStep() {
        return new StepBuilder("placeStagingStep", jobRepository)
                .tasklet(placeStagingTasklet, transactionManager)
                .build();
    }

    @Bean
    public ItemReader<PlaceStaging> placeStagingReader() {
        return new JpaPagingItemReaderBuilder<PlaceStaging>()
                .name("placeStagingReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(100)
                .queryString("SELECT p FROM PlaceStaging p")
                .build();
    }

    @Bean
    public Step placeProcessStep() {
        return new StepBuilder("placeProcessStep", jobRepository)
                .<PlaceStaging, Place>chunk(100, transactionManager)
                .reader(placeStagingReader())
                .processor(placeGeocodeProcessor)
                .writer(placeWriter)
                .build();
    }

    @Bean
    public Job placeDataJob() {
        return new JobBuilder("placeDataJob", jobRepository)
                .start(placeStagingStep()) // 1단계 Step
                .next(placeProcessStep())   // 2단계 Step
                .build();
    }
}
